package org.beifengtz.jvmm.convey.handler;

import com.google.common.base.Stopwatch;
import com.google.gson.JsonSyntaxException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.logging.InternalLogger;
import org.beifengtz.jvmm.common.exception.AuthenticationFailedException;
import org.beifengtz.jvmm.convey.GlobalStatus;
import org.beifengtz.jvmm.convey.channel.JvmmSocketChannel;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:53 2021/5/17
 *
 * @author beifengtz
 */
public abstract class JvmmRequestHandler extends SimpleChannelInboundHandler<String> {

    private final Stopwatch stopWatch = Stopwatch.createUnstarted();

    public JvmmRequestHandler() {
        super(false);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ((JvmmSocketChannel) ctx.channel()).handleActive(ctx.executor());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ((JvmmSocketChannel) ctx.channel()).cleanup();
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        try {
            stopWatch.reset().start();
            JvmmRequest request = JvmmRequest.parseFrom(msg);
            ((JvmmSocketChannel) ctx.channel()).handleRequest(request);
            if (!request.isHeartbeat()) {
                logger().debug(String.format("%s %dms", request.getType(), stopWatch.stop().elapsed(TimeUnit.MILLISECONDS)));
            }
        } catch (JsonSyntaxException e) {
            ctx.channel().writeAndFlush(JvmmResponse.create()
                    .setStatus(GlobalStatus.JVMM_STATUS_UNRECOGNIZED_CONTENT.name())
                    .setMessage(e.getMessage())
                    .toJsonStr());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof AuthenticationFailedException) {
            ctx.channel().writeAndFlush(JvmmResponse.create()
                    .setStatus(GlobalStatus.JVMM_STATUS_AUTHENTICATION_FAILED.name())
                    .setMessage("Authentication failed.")
                    .toJsonStr());
            ctx.close();
        } else if (cause instanceof IOException) {
            logger().debug(cause.toString());
        } else {
            logger().error(cause.toString(), cause);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        ((JvmmSocketChannel) ctx.channel()).handleUserEvent(evt);
    }

    public abstract InternalLogger logger();
}
