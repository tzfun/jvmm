package org.beifengtz.jvmm.convey.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.convey.entity.JvmmMsg;

import java.util.List;

/**
 * description TODO
 * date 16:31 2023/9/8
 *
 * @author beifengtz
 */
public class JvmmMsgEncoder extends MessageToMessageEncoder<JvmmMsg> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(JvmmMsgEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, JvmmMsg msg, List<Object> out) throws Exception {
        try {
            out.add(msg.serialize());
        } catch (Exception e) {
            logger.error("Encode jvmm message failed:" + e.getMessage(), e);
        }
    }
}
