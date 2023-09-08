package org.beifengtz.jvmm.convey.socket;

import com.google.gson.JsonObject;
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
import org.beifengtz.jvmm.common.exception.RpcStatusException;
import org.beifengtz.jvmm.common.exception.SocketExecuteException;
import org.beifengtz.jvmm.common.util.SignatureUtil;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleDecrypt;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleEncrypt;
import org.beifengtz.jvmm.convey.channel.ChannelUtil;
import org.beifengtz.jvmm.convey.channel.JvmmServerChannelInitializer;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.enums.RpcStatus;
import org.beifengtz.jvmm.convey.enums.RpcType;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
    private CompletableFuture<Boolean> openFuture;
    private volatile Channel channel;

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
                channel.writeAndFlush(JvmmRequest.create().setType(RpcType.JVMM_HEARTBEAT));
                workGroup.next().schedule(this, 5, TimeUnit.SECONDS);
            }
        }
    }

    class SocketResponseHandler extends SimpleChannelInboundHandler<JvmmResponse> implements HandlerProvider {

        private static final String HANDLER_NAME = "jvmmSocketHandler";

        @Override
        public ChannelHandler getHandler() {
            return this;
        }

        @Override
        public String getName() {
            return HANDLER_NAME;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, JvmmResponse response) throws Exception {
            RpcType type = response.getType();
            if (response.getStatus() == RpcStatus.JVMM_STATUS_AUTHENTICATION_FAILED) {
                openFuture.complete(false);
                close();
                return;
            }
            if (RpcType.JVMM_BUBBLE == type) {
                JsonObject data = response.getData().getAsJsonObject();
                String key = data.get("key").getAsString();
                int seed = data.get("seed").getAsInt();

                ctx.pipeline()
                        .addAfter(ctx.executor(), ChannelUtil.STRING_DECODER_HANDLER,
                                JvmmServerChannelInitializer.JVMM_BUBBLE_ENCODER, new JvmmBubbleEncrypt(seed, key))
                        .addAfter(ctx.executor(), ChannelUtil.STRING_DECODER_HANDLER,
                                JvmmServerChannelInitializer.JVMM_BUBBLE_DECODER, new JvmmBubbleDecrypt(seed, key));

                JvmmRequest authReq = JvmmRequest.create().setType(RpcType.JVMM_AUTHENTICATION);

                if (authAccount != null && authPassword != null) {
                    JsonObject reqData = new JsonObject();
                    reqData.addProperty("account", SignatureUtil.MD5(authAccount));
                    reqData.addProperty("password", SignatureUtil.MD5(authPassword));
                    authReq.setData(reqData);
                }

                ctx.pipeline().writeAndFlush(authReq);

                if (keepAlive) {
                    workGroup.next().execute(new HeartBeatHandler());
                }

            } else if (RpcType.JVMM_AUTHENTICATION == type) {
                openFuture.complete(RpcStatus.JVMM_STATUS_OK == response.getStatus());
            } else {
                listeners.forEach(o -> o.onMessage(response));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (cause instanceof IOException) {
                logger.debug(cause.toString());
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
     * @param workerGroup  连接工作线程组，建议使用{@link ChannelUtil#newEventLoopGroup}方法获得
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

    public void unregisterListener(MsgReceiveListener... listeners) {
        for (MsgReceiveListener l : listeners) {
            this.listeners.remove(l);
        }
    }

    public void unregisterAllListener() {
        this.listeners.clear();
    }

    public void registerCloseListener(CloseListener closeListener) {
        this.closeListener = closeListener;
    }

    public CompletableFuture<Boolean> connect() {
        if (state != State.CONNECTED) {
            openFuture = new CompletableFuture<>();
            Bootstrap b = new Bootstrap();
            b.group(workGroup)
                    .channel(ChannelUtil.channelClass(workGroup))
                    .handler(new JvmmServerChannelInitializer(new SocketResponseHandler()));
            ChannelFuture connectFuture = b.connect(host, port);
            connectFuture.addListener(f -> {
                if (f.isSuccess()) {
                    channel = connectFuture.channel();
                    channel.closeFuture().addListener(future -> {
                        state = State.CLOSED;
                        if (closeListener != null) {
                            closeListener.onclose();
                        }
                    });
                } else {
                    openFuture.completeExceptionally(f.cause());
                }
            });

            state = State.CONNECTED;
        } else {
            logger.warn("Jvmm socket connector is already connected.");
        }
        return openFuture;
    }

    public void sendOnly(JvmmRequest request) {
        if (isConnected()) {
            channel.writeAndFlush(request).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            throw new IllegalStateException("Jvmm socket has disconnected");
        }
    }

    public CompletableFuture<JvmmResponse> send(JvmmRequest request) {
        CompletableFuture<JvmmResponse> future = new CompletableFuture<>();
        if (isConnected()) {
            long contextId = request.getContextId();
            MsgReceiveListener listener = response -> {
                if (response.getContextId() == contextId) {
                    future.complete(response);
                }
            };
            registerListener(listener);
            channel.writeAndFlush(request).addListener(f -> {
                if (!f.isSuccess()) {
                    future.completeExceptionally(f.cause());
                }
            });
            future.whenComplete((r,t) -> unregisterListener(listener));
        } else {
            future.completeExceptionally(new IllegalStateException("Jvmm socket has disconnected"));
        }
        return future;
    }

    public String ping() {
        try {
            JvmmRequest request = JvmmRequest.create().setType(RpcType.JVMM_PING);
            long contextId = request.getContextId();
            final DefaultPromise<JvmmResponse> promise = new DefaultPromise<>(workGroup.next());
            MsgReceiveListener listener = response -> {
                if (response.getContextId() == contextId) {
                    if (RpcStatus.JVMM_STATUS_OK == response.getStatus()) {
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
                unregisterListener(listener);
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

    public JvmmResponse waitForResponse(JvmmRequest request, long timeout, TimeUnit timeunit) throws RpcStatusException,
            InterruptedException, TimeoutException {
        CompletableFuture<JvmmResponse> future = new CompletableFuture<>();
        long contextId = request.getContextId();
        MsgReceiveListener listener = response -> {
            if (contextId == response.getContextId()) {
                if (RpcStatus.JVMM_STATUS_OK == response.getStatus()) {
                    future.complete(response);
                } else {
                    future.completeExceptionally(new RpcStatusException(response.getMessage(), response.getStatus().name()));
                }
            }
        };
        registerListener(listener);
        send(request);
        try {
            return future.get(timeout, timeunit);

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 一次性连接请求
     *
     * @param group    executor
     * @param host     地址，比如：127.0.0.1
     * @param port     端口
     * @param request  {@link JvmmRequest}
     * @param timeout  超时时间
     * @param timeunit 超时单位
     * @return {@link JvmmResponse}
     * @throws RpcStatusException   响应状态码错误时抛出，非 {@link RpcStatus#JVMM_STATUS_OK} 状态均会抛出
     * @throws InterruptedException 中断异常，不可控
     * @throws TimeoutException     超时未返回后抛出
     */
    public static JvmmResponse waitForResponse(EventLoopGroup group, String host, int port, JvmmRequest request,
                                               long timeout, TimeUnit timeunit)
            throws RpcStatusException, InterruptedException, TimeoutException {
        if (group == null) {
            throw new IllegalArgumentException("Can not execute one request with null event loop group.");
        }
        try (JvmmConnector connector = newInstance(host, port, group)) {
            return connector.waitForResponse(request, timeout, timeunit);
        }
    }
}
