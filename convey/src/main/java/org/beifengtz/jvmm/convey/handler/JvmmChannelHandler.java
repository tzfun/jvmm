package org.beifengtz.jvmm.convey.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.beifengtz.jvmm.common.exception.AuthenticationFailedException;
import org.beifengtz.jvmm.common.exception.InvalidMsgException;
import org.beifengtz.jvmm.convey.GlobalStatus;
import org.beifengtz.jvmm.convey.GlobalType;
import org.beifengtz.jvmm.convey.auth.JvmmBubble;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleDecrypt;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleEncrypt;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.convey.channel.JvmmServerChannelInitializer;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:53 2021/5/17
 *
 * @author beifengtz
 */
public abstract class JvmmChannelHandler extends SimpleChannelInboundHandler<String> {

    protected final JvmmBubble bubble = new JvmmBubble();

    public JvmmChannelHandler() {
        super(false);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        int seed = bubble.generateSeed();
        JsonObject data = new JsonObject();
        data.addProperty("seed", seed);
        data.addProperty("key", bubble.getKey());

        JvmmResponse bubbleResp = JvmmResponse.create()
                .setType(GlobalType.JVMM_TYPE_BUBBLE)
                .setStatus(GlobalStatus.JVMM_STATUS_OK)
                .setData(data);
        ctx.writeAndFlush(bubbleResp.serialize());

        ctx.pipeline()
                .addAfter(ctx.executor(), ChannelInitializers.STRING_DECODER_HANDLER,
                        JvmmServerChannelInitializer.JVMM_BUBBLE_ENCODER, new JvmmBubbleEncrypt(seed, bubble.getKey()))
                .addAfter(ctx.executor(), ChannelInitializers.STRING_DECODER_HANDLER,
                        JvmmServerChannelInitializer.JVMM_BUBBLE_DECODER, new JvmmBubbleDecrypt(seed, bubble.getKey()));

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        cleanup(ctx);
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        try {
            long startTime = System.currentTimeMillis();
            JvmmRequest request = JvmmRequest.parseFrom(msg);
            if (request.getType() == null) {
                return;
            }
            if (GlobalType.JVMM_TYPE_PING.name().equals(request.getType())) {
                ctx.channel().writeAndFlush(JvmmResponse.create()
                        .setType(GlobalType.JVMM_TYPE_PONG)
                        .setStatus(GlobalStatus.JVMM_STATUS_OK)
                        .serialize());
            } else {
                handleRequest(ctx, request);
            }
            if (!request.isHeartbeat()) {
                logger().debug(String.format("%s %dms", request.getType(), System.currentTimeMillis() - startTime));
            }
        } catch (AuthenticationFailedException e) {
            ctx.channel().writeAndFlush(JvmmResponse.create()
                    .setType(GlobalType.JVMM_TYPE_AUTHENTICATION)
                    .setStatus(GlobalStatus.JVMM_STATUS_AUTHENTICATION_FAILED.name())
                    .setMessage("Authentication failed.")
                    .serialize());
            ctx.close();
            logger().debug("Channel closed by auth failed");
        } catch (JsonSyntaxException e) {
            ctx.channel().writeAndFlush(JvmmResponse.create()
                    .setType(GlobalType.JVMM_TYPE_HANDLE_MSG)
                    .setStatus(GlobalStatus.JVMM_STATUS_UNRECOGNIZED_CONTENT.name())
                    .setMessage(e.getMessage())
                    .serialize());
        } catch (Throwable e) {
            ctx.fireExceptionCaught(e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof InvalidMsgException) {
            logger().error("Invalid message verify, seed: {}, ip: {}", ((InvalidMsgException) cause).getSeed(), JvmmConnector.getIpByCtx(ctx));
            ctx.close();
            logger().debug("Channel closed by message verify");
        } else if (cause instanceof IOException) {
            logger().debug(cause.toString());
        } else if (cause instanceof TooLongFrameException) {
            logger().warn("{} | {}", cause.getMessage(), JvmmConnector.getIpByCtx(ctx));
            ctx.close();
        } else {
            logger().error(cause.toString(), cause);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                handleIdle(ctx);
            }
        }
    }

    public abstract Logger logger();

    public abstract void handleRequest(ChannelHandlerContext ctx, JvmmRequest reqMsg);

    public abstract void handleIdle(ChannelHandlerContext ctx);

    public abstract void cleanup(ChannelHandlerContext ctx) throws Exception;
}
