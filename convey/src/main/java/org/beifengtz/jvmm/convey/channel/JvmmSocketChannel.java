package org.beifengtz.jvmm.convey.channel;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.EventExecutor;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;

import java.nio.channels.ClosedChannelException;

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

    protected static final ChannelFutureListener FIRE_EXCEPTION_ON_FAILURE = f -> {
        if (!f.isSuccess() && !(f.cause() instanceof ClosedChannelException))
            f.channel().pipeline().fireExceptionCaught(f.cause());
    };

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return super.writeAndFlush(msg).addListener(FIRE_EXCEPTION_ON_FAILURE);
    }

    public void handleActive(EventExecutor executor) {

    }

    public void handleUserEvent(Object evt) {

    }

    public abstract void handleRequest(JvmmRequest reqMsg);

    public abstract void handleIdle();

    public abstract void cleanup();
}
