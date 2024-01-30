package org.beifengtz.jvmm.convey.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;

import static org.beifengtz.jvmm.convey.channel.ChannelUtil.AGGREGATOR_HANDLER;
import static org.beifengtz.jvmm.convey.channel.ChannelUtil.HTTP_CODEC_HANDLER;
import static org.beifengtz.jvmm.convey.channel.ChannelUtil.IDLE_STATE_HANDLER;

/**
 * description: TODO
 * date: 9:46 2024/1/30
 *
 * @author beifengtz
 */
public class HttpClientChannelInitializer extends ChannelInitializer<Channel> {

    protected HandlerProvider provider;

    protected SslContext sslCtx;

    public HttpClientChannelInitializer(HandlerProvider provider) {
        this(provider, null);
    }

    public HttpClientChannelInitializer(HandlerProvider provider, SslContext sslCtx) {
        this.provider = provider;
        this.sslCtx = sslCtx;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        if (provider.getIdleSeconds() > 0)
            p.addLast(provider.getGroup(), IDLE_STATE_HANDLER, new IdleStateHandler(provider.getIdleSeconds(), 0, 0));
        if (sslCtx != null)
            p.addLast(provider.getGroup(), sslCtx.newHandler(ch.alloc()));
        p.addLast(provider.getGroup(), HTTP_CODEC_HANDLER, new HttpClientCodec());
        p.addLast(provider.getGroup(), AGGREGATOR_HANDLER, new HttpObjectAggregator(1048576));
        p.addLast(provider.getGroup(), provider.getName(), provider.getHandler());
    }
}
