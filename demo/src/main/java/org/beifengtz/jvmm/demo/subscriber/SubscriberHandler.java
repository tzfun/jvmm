package org.beifengtz.jvmm.demo.subscriber;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.convey.handler.HttpChannelHandler;

/**
 * description TODO
 * date 23:20 2023/6/22
 *
 * @author beifengtz
 */
public class SubscriberHandler extends HttpChannelHandler {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SubscriberHandler.class);

    @Override
    public InternalLogger logger() {
        return logger;
    }

    @Override
    protected boolean handleBefore(ChannelHandlerContext ctx, String uri, FullHttpRequest msg) {
        return true;
    }

    @Override
    protected void handleFinally(ChannelHandlerContext ctx, FullHttpRequest msg) {

    }
}
