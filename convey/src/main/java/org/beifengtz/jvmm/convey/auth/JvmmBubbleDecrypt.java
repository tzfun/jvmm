package org.beifengtz.jvmm.convey.auth;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.beifengtz.jvmm.common.exception.InvalidMsgException;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.util.SignatureUtil;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: TODO
 * </p> 3|sxd{}
 * <p>
 * Created in 8:17 下午 2021/5/29
 *
 * @author beifengtz
 */
public class JvmmBubbleDecrypt extends MessageToMessageDecoder<String> {

    private static final Logger logger = LoggerFactory.logger(JvmmBubbleDecrypt.class);

    private final String key;
    private final AtomicInteger seedCounter;

    public JvmmBubbleDecrypt(int seed, String key) {
        seedCounter = new AtomicInteger(seed);
        this.key = key;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        try {
            int idx = msg.indexOf("|");
            if (idx < 0) {
                return;
            }
            int authCodeLen = Integer.parseInt(msg.substring(0, idx));
            int msgStart = idx + authCodeLen + 1;
            String authCode = msg.substring(idx + 1, msgStart);

            String seed = SignatureUtil.AESDecrypt(authCode, key);
            if (seed != null && Integer.parseInt(seed) == seedCounter.get()) {
                seedCounter.incrementAndGet();
                out.add(msg.substring(msgStart));
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
