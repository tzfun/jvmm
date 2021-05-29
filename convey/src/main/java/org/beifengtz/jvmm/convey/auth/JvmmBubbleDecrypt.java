package org.beifengtz.jvmm.convey.auth;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.beifengtz.jvmm.common.exception.InvalidMsgException;
import org.beifengtz.jvmm.tools.util.SignatureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 8:17 下午 2021/5/29
 *
 * @author beifengtz
 */
public class JvmmBubbleDecrypt extends MessageToMessageDecoder<String> {

    private static final Logger logger = LoggerFactory.getLogger(JvmmBubbleDecrypt.class);

    private final String key;
    private final AtomicInteger seedCounter;

    public JvmmBubbleDecrypt(int seed, String key) {
        seedCounter = new AtomicInteger(seed);
        this.key = key;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        String authCode = msg.substring(0, 172);
        try {
            String seed = SignatureUtil.AESDecrypt(authCode, key);
            if (seed != null && Integer.parseInt(seed) == seedCounter.get()) {
                seedCounter.incrementAndGet();
                out.add(msg.substring(172));
            } else {
                throw new InvalidMsgException(seedCounter.get());
            }
        } catch (InvalidMsgException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Jvmm received an invalid msg");
            logger.debug(e.getMessage(), e);
            throw new InvalidMsgException(seedCounter.get());
        }
    }
}
