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
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import org.beifengtz.jvmm.common.exception.SocketExecuteException;
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

    private boolean keepAlive;

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
                        .addAfter(ctx.executor(), StringChannelInitializer.STRING_DECODER_HANDLER,
                                StringChannelInitializer.JVMM_BUBBLE_ENCODER, new JvmmBubbleEncrypt(seed, key))
                        .addAfter(ctx.executor(), StringChannelInitializer.STRING_DECODER_HANDLER,
                                StringChannelInitializer.JVMM_BUBBLE_DECODER, new JvmmBubbleDecrypt(seed, key));

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

    public static JvmmSocketConnector create(String host, int port, EventLoopGroup workerGroup) {
        return create(host, port, false, null, null, workerGroup);
    }

    public static JvmmSocketConnector create(String host, int port, boolean keepAlive, EventLoopGroup workerGroup) {
        return create(host, port, keepAlive, null, null, workerGroup);
    }

    public static JvmmSocketConnector create(String host, int port, boolean keepAlive, String authAccount, String authPassword, EventLoopGroup workerGroup) {
        JvmmSocketConnector connector = new JvmmSocketConnector();
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
            b.group(workGroup).channel(NioSocketChannel.class).handler(new StringChannelInitializer(new SocketResponseHandler()));
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
            waitForResponse(workGroup, JvmmRequest.create().setType(GlobalType.JVMM_TYPE_PING),
                    GlobalType.JVMM_TYPE_PONG.name(), 3, TimeUnit.SECONDS);
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

    public JvmmResponse waitForResponse(JvmmRequest request)
            throws ExecutionException, InterruptedException, TimeoutException {
        return waitForResponse(workGroup, request, request.getType(), 5, TimeUnit.SECONDS);
    }

    public JvmmResponse waitForResponse(EventLoopGroup group, JvmmRequest request)
            throws ExecutionException, InterruptedException, TimeoutException {
        return waitForResponse(group, request, request.getType(), 5, TimeUnit.SECONDS);
    }

    public JvmmResponse waitForResponse(EventLoopGroup group, JvmmRequest request, long timeout, TimeUnit timeunit)
            throws ExecutionException, InterruptedException, TimeoutException {
        return waitForResponse(group, request, request.getType(), timeout, timeunit);
    }

    public JvmmResponse waitForResponse(EventLoopGroup group, JvmmRequest request, String waitType, long timeout, TimeUnit timeunit)
            throws ExecutionException, InterruptedException, TimeoutException {
        if (group == null) {
            throw new IllegalArgumentException("Can not execute one request with null event loop group.");
        }
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
        registerListener(listener);
        if (!isConnected()) {
            throw new IllegalStateException("Socket is closed");
        }
        send(request);
        return promise.get(timeout, timeunit);
    }
}
