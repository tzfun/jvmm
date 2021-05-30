package org.beifengtz.jvmm.convey.channel;

import com.google.gson.JsonObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.EventExecutor;
import org.beifengtz.jvmm.convey.GlobalStatus;
import org.beifengtz.jvmm.convey.GlobalType;
import org.beifengtz.jvmm.convey.auth.JvmmBubble;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleDecrypt;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleEncrypt;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:19 2021/5/17
 *
 * @author beifengtz
 */
public abstract class JvmmSocketChannel extends NioSocketChannel {

    protected EventExecutor handlerExecutor;

    protected JvmmBubble bubble = new JvmmBubble();

    public JvmmSocketChannel(Channel parent, SocketChannel socket) {
        super(parent, socket);
    }

    protected static final ChannelFutureListener FIRE_EXCEPTION_ON_FAILURE = f -> {
        if (!f.isSuccess() && !(f.cause() instanceof ClosedChannelException)){
            f.channel().pipeline().fireExceptionCaught(f.cause());
        }
    };

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return super.writeAndFlush(msg).addListener(FIRE_EXCEPTION_ON_FAILURE);
    }

    public void handleActive(EventExecutor executor) {
        this.handlerExecutor = executor;
        int seed = bubble.generateSeed();
        JsonObject data = new JsonObject();
        data.addProperty("seed", seed);
        data.addProperty("key", bubble.getKey());

        JvmmResponse bubbleResp = JvmmResponse.create()
                .setType(GlobalType.JVMM_TYPE_BUBBLE)
                .setStatus(GlobalStatus.JVMM_STATUS_OK)
                .setData(data);
        writeAndFlush(bubbleResp.serialize());

        pipeline()
                .addAfter(executor, StringChannelInitializer.STRING_DECODER_HANDLER,
                        StringChannelInitializer.JVMM_BUBBLE_ENCODER, new JvmmBubbleEncrypt(seed, bubble.getKey()))
                .addAfter(executor, StringChannelInitializer.STRING_DECODER_HANDLER,
                        StringChannelInitializer.JVMM_BUBBLE_DECODER, new JvmmBubbleDecrypt(seed, bubble.getKey()));
    }

    public void handleUserEvent(Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                handleIdle();
            }
        }
    }

    public abstract void handleRequest(JvmmRequest reqMsg) ;

    public abstract void handleIdle();

    public abstract void cleanup();
}
