package org.beifengtz.jvmm.convey.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.beifengtz.jvmm.convey.entity.JvmmMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * description TODO
 * date 16:31 2023/9/8
 *
 * @author beifengtz
 */
public class JvmmMsgEncoder extends MessageToMessageEncoder<JvmmMsg> {
    private static final Logger logger = LoggerFactory.getLogger(JvmmMsgEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, JvmmMsg msg, List<Object> out) throws Exception {
        try {
            out.add(msg.serialize());
        } catch (Exception e) {
            logger.error("Encode jvmm message failed:" + e.getMessage(), e);
        }
    }
}
