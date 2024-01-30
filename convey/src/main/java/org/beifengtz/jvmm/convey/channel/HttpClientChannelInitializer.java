package org.beifengtz.jvmm.convey.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.base64.Base64Decoder;
import io.netty.handler.codec.base64.Base64Encoder;
import io.netty.handler.codec.compression.SnappyFrameDecoder;
import io.netty.handler.codec.compression.SnappyFrameEncoder;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;

import static org.beifengtz.jvmm.convey.channel.ChannelUtil.AGGREGATOR_HANDLER;
import static org.beifengtz.jvmm.convey.channel.ChannelUtil.BASE64_DECODER;
import static org.beifengtz.jvmm.convey.channel.ChannelUtil.BASE64_ENCODER;
import static org.beifengtz.jvmm.convey.channel.ChannelUtil.HTTP_CODEC_HANDLER;
import static org.beifengtz.jvmm.convey.channel.ChannelUtil.IDLE_STATE_HANDLER;
import static org.beifengtz.jvmm.convey.channel.ChannelUtil.SNAPPY_DECODER;
import static org.beifengtz.jvmm.convey.channel.ChannelUtil.SNAPPY_ENCODER;

/**
 * description: TODO
 * date: 9:46 2024/1/30
 *
 * @author beifengtz
 */
public class HttpClientChannelInitializer extends ChannelInitializer<Channel> {

    protected HandlerProvider provider;
    protected SslContext sslCtx;
    protected String compression;

    public HttpClientChannelInitializer(HandlerProvider provider, SslContext sslCtx, String compression) {
        this.provider = provider;
        this.sslCtx = sslCtx;
        this.compression = compression;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        if (provider.getIdleSeconds() > 0) {
            p.addLast(provider.getGroup(), IDLE_STATE_HANDLER, new IdleStateHandler(provider.getIdleSeconds(), 0, 0));
        }

        if (sslCtx != null) {
            p.addLast(provider.getGroup(), sslCtx.newHandler(ch.alloc()));
        }

        p.addLast(provider.getGroup(), HTTP_CODEC_HANDLER, new HttpClientCodec());
//        if ("snappy".equalsIgnoreCase(compression)) {
//            p.addLast(provider.getGroup(), SNAPPY_ENCODER, new SnappyFrameEncoder());
//            p.addLast(provider.getGroup(), SNAPPY_DECODER, new SnappyFrameDecoder());
//        } else if ("base64".equalsIgnoreCase(compression)) {
//            p.addLast(provider.getGroup(), BASE64_ENCODER, new Base64Encoder());
//            p.addLast(provider.getGroup(), BASE64_DECODER, new Base64Decoder());
//        }
        p.addLast(provider.getGroup(), AGGREGATOR_HANDLER, new HttpObjectAggregator(1048576));
        p.addLast(provider.getGroup(), provider.getName(), provider.getHandler());
    }
}
