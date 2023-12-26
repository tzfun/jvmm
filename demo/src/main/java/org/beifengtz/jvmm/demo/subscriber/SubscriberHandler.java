package org.beifengtz.jvmm.demo.subscriber;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.RequestBody;
import org.beifengtz.jvmm.convey.enums.Method;
import org.beifengtz.jvmm.convey.handler.HttpChannelHandler;
import org.beifengtz.jvmm.core.entity.JvmmData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description TODO
 * date 23:20 2023/6/22
 *
 * @author beifengtz
 */
public class SubscriberHandler extends HttpChannelHandler {
    private static final Logger logger = LoggerFactory.getLogger(SubscriberHandler.class);

    @Override
    public Logger logger() {
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
