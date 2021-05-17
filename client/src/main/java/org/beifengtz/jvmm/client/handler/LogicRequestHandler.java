package org.beifengtz.jvmm.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:15 2021/5/17
 *
 * @author beifengtz
 */
public abstract class LogicRequestHandler  extends SimpleChannelInboundHandler<String> {

    public LogicRequestHandler() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

    }
}
