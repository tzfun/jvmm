package org.beifengtz.jvmm.convey.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.beifengtz.jvmm.convey.entity.JvmmMsg;

import java.util.List;

/**
 * description TODO
 * date 16:31 2023/9/8
 *
 * @author beifengtz
 */
public class JvmmMsgEncoder extends MessageToMessageEncoder<JvmmMsg> {
    @Override
    protected void encode(ChannelHandlerContext ctx, JvmmMsg msg, List<Object> out) throws Exception {
        out.add(msg.serialize());
    }
}
