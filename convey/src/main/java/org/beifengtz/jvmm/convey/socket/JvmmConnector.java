package org.beifengtz.jvmm.convey.socket;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import org.beifengtz.jvmm.common.exception.ErrorStatusException;
import org.beifengtz.jvmm.common.exception.SocketExecuteException;
import org.beifengtz.jvmm.common.util.SignatureUtil;
import org.beifengtz.jvmm.convey.enums.GlobalStatus;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleDecrypt;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleEncrypt;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.convey.channel.JvmmServerChannelInitializer;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * Description: Jvmm通信工具，与Jvmm Server建立通信通道，提供connect、close、send等基础方法
 * </p>
 * <p>
 * Created in 4:57 下午 2021/5/29
 *
 * @author beifengtz
 */
public class JvmmConnector implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(JvmmConnector.class);

    private String host;
    private int port;

    private String authAccount;
    private String authPassword;

    private EventLoopGroup workGroup;
    private volatile State state = State.NONE;
    private DefaultPromise<Boolean> openPromise;
    private Channel channel;

    private boolean keepAlive;

    private final List<MsgReceiveListener> listeners;
    private CloseListener closeListener;

    private JvmmConnector() {
        listeners = new LinkedList<>();
    }

    private enum State {
        NONE, CONNECTED, CLOSED
    }

    public interface MsgReceiveListener {
        void onMessage(JvmmResponse response);
    }

    public interface CloseListener {
        void onclose();
    }

    class HeartBeatHandler implements Runnable {

        @Override
        public void run() {
            if (isConnected()) {
                channel.writeAndFlush(JvmmRequest.create().setType(GlobalType.JVMM_TYPE_HEARTBEAT).serialize());
                workGroup.next().schedule(this, 5, TimeUnit.SECONDS);
            }
        }
    }

    class SocketResponseHandler extends SimpleChannelInboundHandler<String> implements HandlerProvider {

        private static final String handleName = "jvmmSocketRespHandler";

        @Override
        public ChannelHandler getHandler() {
            return this;
        }

        @Override
        public String getName() {
            return handleName;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            JvmmResponse response = null;
            try {
                response = JvmmResponse.parseFrom(msg);
            } catch (JsonSyntaxException e) {
                logger.warn("Jvmm socket connector received an unparseable message: " + msg);
                logger.debug(e.getMessage(), e);
            }
            if (response != null) {
                handleResponse(ctx, response);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (cause instanceof IOException) {
                logger.debug(cause.toString());
            }
        }

        private void handleResponse(ChannelHandlerContext ctx, JvmmResponse response) throws Exception {
            String type = response.getType();
            if (Objects.equals(response.getStatus(), GlobalStatus.JVMM_STATUS_AUTHENTICATION_FAILED.name())) {
                openPromise.trySuccess(false);
                close();
                return;
            }
            if (GlobalType.JVMM_TYPE_BUBBLE.name().equals(type)) {
                JsonObject data = response.getData().getAsJsonObject();
                String key = data.get("key").getAsString();
                int seed = data.get("seed").getAsInt();

                ctx.pipeline()
                        .addAfter(ctx.executor(), ChannelInitializers.STRING_DECODER_HANDLER,
                                JvmmServerChannelInitializer.JVMM_BUBBLE_ENCODER, new JvmmBubbleEncrypt(seed, key))
                        .addAfter(ctx.executor(), ChannelInitializers.STRING_DECODER_HANDLER,
                                JvmmServerChannelInitializer.JVMM_BUBBLE_DECODER, new JvmmBubbleDecrypt(seed, key));

                JvmmRequest authReq = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_AUTHENTICATION);

                if (authAccount != null && authPassword != null) {
                    JsonObject reqData = new JsonObject();
                    reqData.addProperty("account", SignatureUtil.MD5(authAccount));
                    reqData.addProperty("password", SignatureUtil.MD5(authPassword));
                    authReq.setData(reqData);
                }

                ctx.pipeline().writeAndFlush(authReq.serialize());

                if (keepAlive) {
                    workGroup.next().execute(new HeartBeatHandler());
                }

            } else if (GlobalType.JVMM_TYPE_AUTHENTICATION.name().equals(type)) {
                openPromise.trySuccess(GlobalStatus.JVMM_STATUS_OK.name().equals(response.getStatus()));
            } else {
                listeners.forEach(o -> o.onMessage(response));
            }
        }
    }

    public static JvmmConnector newInstance(String host, int port, EventLoopGroup workerGroup) {
        return newInstance(host, port, workerGroup, false, null, null);
    }

    public static JvmmConnector newInstance(String host, int port, EventLoopGroup workerGroup, boolean keepAlive) {
        return newInstance(host, port, workerGroup, keepAlive, null, null);
    }

    /**
     * 新建一个连接器实例向Jvmm Server通信，如果目标Server打开了安全认证，需传入认证账号和密码，如未打开安全认证，可不传
     *
     * @param host         目标Server ip或域名
     * @param port         目标Server端口
     * @param keepAlive    是否自动保持连接活跃。
     *                     true - 自动向Server发送心跳包，直至断开连接，断开连接调用{@link #close()}方法；
     *                     false - 不自动发送心跳包，连接闲置后会自动断开连接。
     * @param authAccount  安全认证账号
     * @param authPassword 安全认证密码
     * @param workerGroup  连接工作线程组，建议使用{@link ChannelInitializers#newEventLoopGroup}方法获得
     * @return {@link JvmmConnector} 实例
     */
    public static JvmmConnector newInstance(String host, int port, EventLoopGroup workerGroup, boolean keepAlive, String authAccount, String authPassword) {
        JvmmConnector connector = new JvmmConnector();
        connector.host = host;
        connector.port = port;
        connector.authAccount = authAccount;
        connector.authPassword = authPassword;
        connector.workGroup = workerGroup;
        connector.keepAlive = keepAlive;
        return connector;
    }

    public void registerListener(MsgReceiveListener listener) {
        this.listeners.add(listener);
    }

    public void registerListener(List<MsgReceiveListener> listeners) {
        this.listeners.addAll(listeners);
    }

    public void removeListener(MsgReceiveListener... listeners) {
        for (MsgReceiveListener l : listeners) {
            this.listeners.remove(l);
        }
    }

    public void removeAllListener() {
        this.listeners.clear();
    }

    public void registerCloseListener(CloseListener closeListener) {
        this.closeListener = closeListener;
    }

    public Future<Boolean> connect() {
        if (state != State.CONNECTED) {
            openPromise = new DefaultPromise<>(workGroup.next());
            Bootstrap b = new Bootstrap();
            b.group(workGroup).channel(ChannelInitializers.channelClass(workGroup)).handler(new JvmmServerChannelInitializer(new SocketResponseHandler()));
            channel = b.connect(host, port).channel();
            channel.closeFuture().addListener(future -> {
                state = State.CLOSED;
                if (closeListener != null) {
                    closeListener.onclose();
                }
            });
            state = State.CONNECTED;
        } else {
            logger.warn("Jvmm socket connector is already connected.");
        }
        return openPromise;
    }

    public ChannelFuture send(JvmmRequest request) {
        if (isConnected()) {
            return channel.writeAndFlush(request.serialize()).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            throw new IllegalStateException("Jvmm socket has disconnected");
        }
    }

    public String ping() {
        try {
            JvmmRequest request = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_PING);

            final DefaultPromise<JvmmResponse> promise = new DefaultPromise<>(workGroup.next());
            MsgReceiveListener listener = response -> {
                String waitFor = GlobalType.JVMM_TYPE_PONG.name();

                if (waitFor.equals(response.getType())) {
                    if (GlobalStatus.JVMM_STATUS_OK.name().equals(response.getStatus())) {
                        promise.trySuccess(response);
                    } else {
                        promise.tryFailure(new Exception(response.getMessage()));
                    }
                }
            };
            registerListener(listener);
            try {
                send(request);
                promise.get(3, TimeUnit.SECONDS);
            } finally {
                removeListener(listener);
            }
        } catch (Exception e) {
            throw new SocketExecuteException(e.getMessage(), e);
        }
        return "pong";
    }

    public boolean isConnected() {
        return state == State.CONNECTED && channel != null && channel.isActive();
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.close();
        }
    }

    public void close(ChannelPromise promise) {
        if (channel != null) {
            channel.close(promise);
        }
    }

    public JvmmResponse waitForResponse(JvmmRequest request)
            throws ErrorStatusException, InterruptedException, TimeoutException {
        return waitForResponse(request, 5, TimeUnit.SECONDS);
    }

    public JvmmResponse waitForResponse(JvmmRequest request, long timeout, TimeUnit timeunit) throws ErrorStatusException,
            InterruptedException, TimeoutException {
        return waitForResponse(request, null, timeout, timeunit);
    }

    public JvmmResponse waitForResponse(JvmmRequest request, String waitType, long timeout, TimeUnit timeunit)
            throws ErrorStatusException, InterruptedException, TimeoutException {
        final DefaultPromise<JvmmResponse> promise = new DefaultPromise<>(workGroup.next());
        MsgReceiveListener listener = response -> {
            String waitFor = waitType;
            if (waitFor == null) {
                waitFor = request.getType();
            }
            if (waitFor.equals(response.getType())) {
                if (GlobalStatus.JVMM_STATUS_OK.name().equals(response.getStatus())) {
                    promise.trySuccess(response);
                } else {
                    promise.tryFailure(new ErrorStatusException(response.getMessage(), response.getStatus()));
                }
            }
        };
        registerListener(listener);
        send(request);
        if (promise.await(timeout, timeunit)) {
            if (promise.isSuccess()) {
                return promise.getNow();
            } else {
                if (promise.cause() instanceof ErrorStatusException) {
                    throw (ErrorStatusException) promise.cause();
                } else {
                    throw new RuntimeException(promise.cause());
                }
            }
        } else {
            throw new TimeoutException("request time out");
        }
    }

    public static JvmmResponse waitForResponse(EventLoopGroup group, String address, JvmmRequest request)
            throws ErrorStatusException, InterruptedException, TimeoutException {
        return waitForResponse(group, address, request, request.getType(), 5, TimeUnit.SECONDS);
    }

    public static JvmmResponse waitForResponse(EventLoopGroup group, String address, JvmmRequest request, long timeout, TimeUnit timeunit)
            throws ErrorStatusException, InterruptedException, TimeoutException {
        return waitForResponse(group, address, request, request.getType(), timeout, timeunit);
    }

    /**
     * 一次性连接请求
     *
     * @param group    executor
     * @param address  地址，比如：127.0.0.1:5010
     * @param request  {@link JvmmRequest}
     * @param waitType 监听返回类型，如果为null则默认监听请求的类型。见：{@link GlobalType}
     * @param timeout  超时时间
     * @param timeunit 超时单位
     * @return {@link JvmmResponse}
     * @throws ErrorStatusException 响应状态码错误时抛出，非 {@link GlobalStatus#JVMM_STATUS_OK} 状态均会抛出
     * @throws InterruptedException 中断异常，不可控
     * @throws TimeoutException     超时未返回后抛出
     */
    public static JvmmResponse waitForResponse(EventLoopGroup group, String address, JvmmRequest request, String waitType, long timeout, TimeUnit timeunit)
            throws ErrorStatusException, InterruptedException, TimeoutException {
        if (group == null) {
            throw new IllegalArgumentException("Can not execute one request with null event loop group.");
        }
        String[] split = address.split(":");
        try (JvmmConnector connector = newInstance(split[0], Integer.parseInt(split[1]), group)) {
            return connector.waitForResponse(request, waitType, timeout, timeunit);
        }
    }

    public static String getIpByCtx(ChannelHandlerContext ctx) {
        String ip = null;
        try {
            SocketAddress socketAddress = ctx.channel().remoteAddress();
            if (socketAddress != null) {
                ip = ((InetSocketAddress) socketAddress).getAddress().getHostAddress();
            }
        } catch (Exception ignored) {
        }
        return ip;
    }
}
