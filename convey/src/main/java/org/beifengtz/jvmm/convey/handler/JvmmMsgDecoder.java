package org.beifengtz.jvmm.convey.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.convey.entity.JvmmMsg;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;

import java.util.List;

/**
 * description TODO
 * date 16:31 2023/9/8
 *
 * @author beifengtz
 */
public class JvmmMsgDecoder extends MessageToMessageDecoder<ByteBuf> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(JvmmMsgDecoder.class);

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
        out.add(jvmmMsg);
    }
}
