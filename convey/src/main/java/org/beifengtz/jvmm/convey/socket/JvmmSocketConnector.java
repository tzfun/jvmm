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
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import org.beifengtz.jvmm.common.exception.AuthenticationFailedException;
import org.beifengtz.jvmm.common.exception.InvalidMsgException;
import org.beifengtz.jvmm.convey.GlobalStatus;
import org.beifengtz.jvmm.convey.GlobalType;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleDecrypt;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleEncrypt;
import org.beifengtz.jvmm.convey.channel.StringChannelInitializer;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
public class JvmmSocketConnector {

    private static final Logger logger = LoggerFactory.getLogger(JvmmSocketConnector.class);

    private String host;
    private int port;

    private String authAccount;
    private String authPassword;

    private EventLoopGroup workGroup;

    private State state = State.NONE;

    private DefaultPromise<Boolean> openPromise;
    private Channel channel;

    private final List<MsgReceiveListener> listeners;

    private JvmmSocketConnector() {
        listeners = new LinkedList<>();
    }

    private enum State {
        NONE, CONNECTED, CLOSED
    }

    public interface MsgReceiveListener {
        void onMessage(JvmmResponse response);
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
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if (cause instanceof AuthenticationFailedException) {
                ctx.channel().writeAndFlush(JvmmResponse.create()
                        .setStatus(GlobalStatus.JVMM_STATUS_AUTHENTICATION_FAILED.name())
                        .setMessage("Authentication failed.")
                        .toJsonStr());
                ctx.close();
            } else if (cause instanceof InvalidMsgException) {
                logger.error("Invalid message verify, seed: " + ((InvalidMsgException) cause).getSeed());
                ctx.close();
            } else if (cause instanceof IOException) {
                logger.debug(cause.toString());
            } else {
                logger.error(cause.toString(), cause);
            }
        }

        private void handleResponse(ChannelHandlerContext ctx, JvmmResponse response) {
            String type = response.getType();
            if (GlobalType.JVMM_TYPE_BUBBLE.name().equals(type)) {

                JsonObject data = response.getData().getAsJsonObject();
                String key = data.get("key").getAsString();
                int seed = data.get("seed").getAsInt();

                ctx.pipeline()
                        .addAfter(ctx.executor(), StringChannelInitializer.STRING_DECODER_HANDLER,
                                StringChannelInitializer.JVMM_BUBBLE_ENCODER, new JvmmBubbleEncrypt(seed, key))
                        .addAfter(ctx.executor(), StringChannelInitializer.STRING_DECODER_HANDLER,
                                StringChannelInitializer.JVMM_BUBBLE_DECODER, new JvmmBubbleDecrypt(seed, key));

                if (authAccount != null && authPassword != null) {
                    JsonObject reqData = new JsonObject();
                    reqData.addProperty("account", authAccount);
                    reqData.addProperty("password", authPassword);
                    JvmmRequest request = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_AUTHENTICATION).setData(reqData);
                    ctx.pipeline().writeAndFlush(request.toJsonStr());
                    openPromise.trySuccess(true);
                }

            } else if (GlobalType.JVMM_TYPE_AUTHENTICATION.name().equals(type)) {
                openPromise.trySuccess(GlobalStatus.JVMM_STATUS_OK.name().equals(response.getStatus()));
            } else if (GlobalType.JVMM_TYPE_PING.name().equals(type)) {
                ctx.pipeline().writeAndFlush(JvmmRequest.create().setType(GlobalType.JVMM_TYPE_PONG).toJsonStr());
            } else {
                listeners.forEach(o -> o.onMessage(response));
            }
        }
    }

    public static JvmmSocketConnector create(String host, int port) {
        return create(host, port, null, null, new DefaultEventLoop());
    }

    public static JvmmSocketConnector create(String host, int port, String authAccount, String authPassword) {
        return create(host, port, authAccount, authPassword, new DefaultEventLoop());
    }

    public static JvmmSocketConnector create(String host, int port, String authAccount, String authPassword, EventLoopGroup workerGroup) {
        JvmmSocketConnector connector = new JvmmSocketConnector();
        connector.host = host;
        connector.port = port;
        connector.authAccount = authAccount;
        connector.authPassword = authPassword;
        connector.workGroup = workerGroup;
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
            b.group(workGroup).channel(NioSocketChannel.class).handler(new StringChannelInitializer(new SocketResponseHandler()));
            channel = b.connect(host, port).channel();
            channel.closeFuture().addListener(future -> state = State.CLOSED);
        } else {
            logger.warn("Jvmm socket connector is already connected.");
        }
        return openPromise;
    }

    public ChannelFuture send(JvmmRequest request) {
        if (isConnected()) {
            return channel.writeAndFlush(request.toJsonStr()).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            throw new IllegalStateException("Jvmm socket has disconnected");
        }
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

    public JvmmResponse waitForResponse(EventLoopGroup group, JvmmRequest request, String waitType, long timeout, TimeUnit timeunit) throws Exception {
        final DefaultPromise<JvmmResponse> promise = new DefaultPromise<>(group.next());
        MsgReceiveListener listener = response -> {
            if (waitType.equals(response.getType())) {
                promise.trySuccess(response);
            }
        };
        registerListener(listener);
        if (!isConnected()) {
            if (!connect().await(timeout, timeunit)) {
                throw new TimeoutException("Jvmm socket connect time out");
            }
        }
        send(request);
        return promise.get(timeout, timeunit);
    }
}
