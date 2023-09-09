package org.beifengtz.jvmm.convey.auth;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.beifengtz.jvmm.common.util.CodingUtil;
import org.beifengtz.jvmm.common.util.SignatureUtil;
import org.beifengtz.jvmm.convey.entity.JvmmMsg;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: 对消息进行加密
 * </p>
 * <p>
 * Created in 8:17 下午 2021/5/29
 *
 * @author beifengtz
 */
public class JvmmBubbleEncoder extends MessageToMessageEncoder<ByteBuf> {

    private final String key;
    private final AtomicInteger seedCounter;

    public JvmmBubbleEncoder(int seed, String key) {
        seedCounter = new AtomicInteger(seed);
        this.key = key;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int seed = seedCounter.getAndIncrement();

        //  先对消息进行加密
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        msg.release();
        ByteBuf safeMsg = Unpooled.wrappedBuffer(SignatureUtil.AESEncrypt(bytes, key));

        //  再记录步长
        CompositeByteBuf safeMsgByteBuf = Unpooled.compositeBuffer();
        safeMsgByteBuf.addComponent(true, Unpooled.wrappedBuffer(new byte[]{JvmmMsg.MSG_FLAG_CRC}));
        safeMsgByteBuf.addComponent(true, Unpooled.wrappedBuffer(CodingUtil.intToByteArray(seed)));

        //  合并消息
        safeMsgByteBuf.addComponent(true, safeMsg);
        out.add(safeMsgByteBuf);
    }
}
