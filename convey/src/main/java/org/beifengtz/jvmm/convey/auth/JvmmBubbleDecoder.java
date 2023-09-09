package org.beifengtz.jvmm.convey.auth;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.beifengtz.jvmm.common.util.CodingUtil;
import org.beifengtz.jvmm.common.util.SignatureUtil;
import org.beifengtz.jvmm.convey.entity.JvmmMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: 对消息进行解密，并验证步长
 * </p>
 * Created in 8:17 下午 2021/5/29
 *
 * @author beifengtz
 */
public class JvmmBubbleDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(JvmmBubbleDecoder.class);

    private final String key;
    private final AtomicInteger seedCounter;

    public JvmmBubbleDecoder(int seed, String key) {
        seedCounter = new AtomicInteger(seed);
        this.key = key;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        try {
            byte flag = msg.readByte();
            if (flag != JvmmMsg.MSG_FLAG_CRC) {
                logger.warn("Unable to receive message: invalid jvmm message flag");
                ctx.close();
                return;
            }
            byte[] cSeed = new byte[4];
            msg.readBytes(cSeed);
            int seed = CodingUtil.byteArrayToInt(cSeed);
            if (seed == seedCounter.getAndIncrement()) {
                byte[] content = new byte[msg.readableBytes()];
                msg.readBytes(content);
                byte[] srcMsg = SignatureUtil.AESDecrypt(content, key);
                out.add(Unpooled.wrappedBuffer(srcMsg));
            } else {
                logger.warn("Unable to receive message: invalid jvmm message seed");
                ctx.close();
            }
        } catch (Exception e) {
            logger.warn("Message decoding failed", e);
            ctx.close();
        }
    }
}
