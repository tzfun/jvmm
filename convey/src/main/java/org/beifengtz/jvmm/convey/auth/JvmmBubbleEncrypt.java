package org.beifengtz.jvmm.convey.auth;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.beifengtz.jvmm.tools.util.SignatureUtil;

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
public class JvmmBubbleEncrypt extends MessageToMessageEncoder<String> {

    private final String key;
    private final AtomicInteger seedCounter;

    public JvmmBubbleEncrypt(int seed, String key) {
        seedCounter = new AtomicInteger(seed);
        this.key = key;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        String authCode = SignatureUtil.AESEncrypt(String.valueOf(seedCounter.get()), key);
        seedCounter.incrementAndGet();
        out.add(authCode + msg);
    }
}
