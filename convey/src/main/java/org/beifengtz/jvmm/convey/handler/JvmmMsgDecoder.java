package org.beifengtz.jvmm.convey.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.beifengtz.jvmm.convey.entity.JvmmMsg;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * description TODO
 * date 16:31 2023/9/8
 *
 * @author beifengtz
 */
public class JvmmMsgDecoder extends MessageToMessageDecoder<ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(JvmmMsgDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte flag = msg.getByte(0);
        JvmmMsg jvmmMsg;
        if (flag == JvmmMsg.MSG_FLAG_REQUEST) {
            jvmmMsg = JvmmRequest.parseFrom(msg);
        } else if (flag == JvmmMsg.MSG_FLAG_RESPONSE) {
            jvmmMsg = JvmmResponse.parseFrom(msg);
        } else {
            ctx.close();
            logger.warn("Unable to decode illegal message. {}", ctx.channel());
            return;
        }
        msg.release();
        out.add(jvmmMsg);
    }
}
