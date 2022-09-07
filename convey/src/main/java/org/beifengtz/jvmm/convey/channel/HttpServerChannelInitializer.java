package org.beifengtz.jvmm.convey.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:19 2022/9/7
 *
 * @author beifengtz
 */
public class HttpServerChannelInitializer extends ChannelInitializer<Channel> {

    public static final String HTTP_SERVER_HANDLER_NAME = "jvmmHttpServerHandler";

    private final HandlerProvider provider;

    public HttpServerChannelInitializer(HandlerProvider provider) {
        this.provider = provider;
    }


    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(ChannelInitializers.IDLE_STATE_HANDLER, new IdleStateHandler(60, 0, 0));

        if (provider.getSslContext() != null) {
            p.addLast(provider.getSslContext().newHandler(ch.alloc()));
        }

        p.addLast(ChannelInitializers.HTTP_CODEC_HANDLER, new HttpServerCodec());
        p.addLast(ChannelInitializers.AGGREGATOR_HANDLER, new HttpObjectAggregator(1048576));
        p.addLast(HTTP_SERVER_HANDLER_NAME, provider.getHandler());
    }
}
