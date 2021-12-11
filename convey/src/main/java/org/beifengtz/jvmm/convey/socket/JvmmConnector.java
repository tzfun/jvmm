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
import org.beifengtz.jvmm.common.exception.SocketExecuteException;
import org.beifengtz.jvmm.convey.GlobalStatus;
import org.beifengtz.jvmm.convey.GlobalType;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleDecrypt;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleEncrypt;
import org.beifengtz.jvmm.convey.channel.JvmmChannelInitializer;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 4:57 下午 2021/5/29
 *
 * @author beifengtz
 */
public class JvmmConnector {

    private static final Logger logger = LoggerFactory.logger(JvmmConnector.class);

    private String host;
    private int port;

    private String authAccount;
    private String authPassword;

    private EventLoopGroup workGroup;
    private State state = State.NONE;
    private DefaultPromise<Boolean> openPromise;
    private Channel channel;

    private boolean keepAlive;

    private final List<MsgReceiveListener> listeners;

    private JvmmConnector() {
        listeners = new LinkedList<>();
    }

    private enum State {
        NONE, CONNECTED, CLOSED
    }

    public interface MsgReceiveListener {
        void onMessage(JvmmResponse response);
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

        private void handleResponse(ChannelHandlerContext ctx, JvmmResponse response) {
            String type = response.getType();
            if (GlobalType.JVMM_TYPE_BUBBLE.name().equals(type)) {

                JsonObject data = response.getData().getAsJsonObject();
                String key = data.get("key").getAsString();
                int seed = data.get("seed").getAsInt();

                ctx.pipeline()
                        .addAfter(ctx.executor(), JvmmChannelInitializer.STRING_DECODER_HANDLER,
                                JvmmChannelInitializer.JVMM_BUBBLE_ENCODER, new JvmmBubbleEncrypt(seed, key))
                        .addAfter(ctx.executor(), JvmmChannelInitializer.STRING_DECODER_HANDLER,
                                JvmmChannelInitializer.JVMM_BUBBLE_DECODER, new JvmmBubbleDecrypt(seed, key));

                if (authAccount != null && authPassword != null) {
                    JsonObject reqData = new JsonObject();
                    reqData.addProperty("account", authAccount);
                    reqData.addProperty("password", authPassword);
                    JvmmRequest request = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_AUTHENTICATION).setData(reqData);
                    ctx.pipeline().writeAndFlush(request.serialize());
                }
                openPromise.trySuccess(true);

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
        return newInstance(host, port, false, null, null, workerGroup);
    }

    public static JvmmConnector newInstance(String host, int port, boolean keepAlive, EventLoopGroup workerGroup) {
        return newInstance(host, port, keepAlive, null, null, workerGroup);
    }

    /**
     * 新建一个连接器实例向Jvmm Server通信，如果目标Server打开了安全认证，需传入认证账号和密码，如未打开安全认证，可不传
     *
     * @param host         目标Server ip或域名
     * @param port         目标Server端口
     * @param keepAlive    是否自动保持连接活跃。<p><p>
     *                     true - 自动向Server发送心跳包，直至断开连接，断开连接调用{@link #close()}方法；<p>
     *                     false - 不自动发送心跳包，连接闲置后会自动断开连接。<p>
     * @param authAccount  安全认证账号
     * @param authPassword 安全认证密码
     * @param workerGroup  连接工作线程组，建议使用{@link JvmmChannelInitializer#newEventLoopGroup(int)}方法获得
     * @return {@link JvmmConnector} 实例
     */
    public static JvmmConnector newInstance(String host, int port, boolean keepAlive, String authAccount, String authPassword, EventLoopGroup workerGroup) {
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

    public Future<Boolean> connect() {
        if (state != State.CONNECTED) {
            openPromise = new DefaultPromise<>(workGroup.next());
            Bootstrap b = new Bootstrap();
            b.group(workGroup).channel(JvmmChannelInitializer.channelClass(workGroup)).handler(new JvmmChannelInitializer(new SocketResponseHandler()));
            channel = b.connect(host, port).channel();
            channel.closeFuture().addListener(future -> state = State.CLOSED);
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

    public static JvmmResponse waitForResponse(EventLoopGroup group, String address, JvmmRequest request)
            throws ExecutionException, InterruptedException, TimeoutException {
        return waitForResponse(group, address, request, request.getType(), 5, TimeUnit.SECONDS);
    }

    public static JvmmResponse waitForResponse(EventLoopGroup group, String address, JvmmRequest request, long timeout, TimeUnit timeunit)
            throws ExecutionException, InterruptedException, TimeoutException {
        return waitForResponse(group, address, request, request.getType(), timeout, timeunit);
    }

    /**
     * 一次性连接
     *
     * @param group    executor
     * @param address  地址，比如：127.0.0.1:5010
     * @param request  {@link JvmmRequest}
     * @param waitType 监听返回类型，如果为null则默认监听请求的类型。见：{@link GlobalType}
     * @param timeout  超时时间
     * @param timeunit 超时单位
     * @return {@link JvmmResponse}
     * @throws ExecutionException   执行时异常
     * @throws InterruptedException 中断异常，不可控
     * @throws TimeoutException     超时未返回后抛出
     */
    public static JvmmResponse waitForResponse(EventLoopGroup group, String address, JvmmRequest request, String waitType, long timeout, TimeUnit timeunit)
            throws ExecutionException, InterruptedException, TimeoutException {
        if (group == null) {
            throw new IllegalArgumentException("Can not execute one request with null event loop group.");
        }
        String[] split = address.split(":");
        JvmmConnector connector = newInstance(split[0], Integer.parseInt(split[1]), group);

        final DefaultPromise<JvmmResponse> promise = new DefaultPromise<>(group.next());
        MsgReceiveListener listener = response -> {
            String waitFor = waitType;
            if (waitFor == null) {
                waitFor = request.getType();
            }
            if (waitFor.equals(response.getType())) {
                if (GlobalStatus.JVMM_STATUS_OK.name().equals(response.getStatus())) {
                    promise.trySuccess(response);
                } else {
                    promise.tryFailure(new Exception(response.getMessage()));
                }
            }
        };
        connector.registerListener(listener);
        if (!connector.isConnected()) {
            throw new IllegalStateException("Socket is closed");
        }
        connector.send(request);
        try {
            return promise.get(timeout, timeunit);
        } finally {
            connector.close();
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
