package org.beifengtz.jvmm.convey.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;
import org.beifengtz.jvmm.convey.handler.JvmmMsgDecoder;
import org.beifengtz.jvmm.convey.handler.JvmmMsgEncoder;

import static org.beifengtz.jvmm.convey.channel.ChannelUtil.CHUNKED_WRITE_HANDLER;
import static org.beifengtz.jvmm.convey.channel.ChannelUtil.IDLE_STATE_HANDLER;
import static org.beifengtz.jvmm.convey.channel.ChannelUtil.JVMM_DECODER;
import static org.beifengtz.jvmm.convey.channel.ChannelUtil.JVMM_ENCODER;
import static org.beifengtz.jvmm.convey.channel.ChannelUtil.LEN_DECODER_HANDLER;
import static org.beifengtz.jvmm.convey.channel.ChannelUtil.LEN_ENCODER_HANDLER;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 17:31 2021/5/17
 *
 * @author beifengtz
 */
public class JvmmServerChannelInitializer extends ChannelInitializer<Channel> {

    public static final String JVMM_BUBBLE_DECODER = "jvmmBubbleDecoder";
    public static final String JVMM_BUBBLE_ENCODER = "jvmmBubbleEncoder";

    private final HandlerProvider provider;

    public JvmmServerChannelInitializer(HandlerProvider provider) {
        this.provider = provider;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        if (provider.getIdleSeconds() > 0) {
            p.addLast(provider.getGroup(), IDLE_STATE_HANDLER, new IdleStateHandler(provider.getIdleSeconds(), 0, 0));
        }
        p.addLast(provider.getGroup(), LEN_DECODER_HANDLER, new LengthFieldBasedFrameDecoder(0xFFFFFF, 0, 4, 0, 4));
        p.addLast(provider.getGroup(), LEN_ENCODER_HANDLER, new LengthFieldPrepender(4));
        p.addLast(provider.getGroup(), JVMM_DECODER, new JvmmMsgDecoder());
        p.addLast(provider.getGroup(), JVMM_ENCODER, new JvmmMsgEncoder());
        p.addLast(provider.getGroup(), provider.getName(), provider.getHandler());
    }
}
