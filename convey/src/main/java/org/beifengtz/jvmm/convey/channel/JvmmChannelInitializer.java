package org.beifengtz.jvmm.convey.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.util.PlatformUtil;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 17:31 2021/5/17
 *
 * @author beifengtz
 */
public class JvmmChannelInitializer extends ChannelInitializer<Channel> {

    private static final Logger logger = LoggerFactory.logger(JvmmChannelInitializer.class);

    public static final String IDLE_STATE_HANDLER = "idleStateHandler";

    public static final String LEN_DECODER_HANDLER = "lenDecoderHandler";
    public static final String LEN_ENCODER_HANDLER = "lenEncoderHandler";
    public static final String STRING_DECODER_HANDLER = "stringDecoderHandler";
    public static final String STRING_ENCODER_HANDLER = "stringEncoderHandler";
    public static final String CHUNKED_WRITE_HANDLER = "chunkedWriteHandler";

    public static final String JVMM_BUBBLE_DECODER = "jvmmBubbleDecoder";
    public static final String JVMM_BUBBLE_ENCODER = "jvmmBubbleEncoder";

    private final HandlerProvider provider;

    public JvmmChannelInitializer(HandlerProvider provider) {
        this.provider = provider;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        if (provider.getReaderIdle() > 0) {
            p.addLast(provider.getGroup(), IDLE_STATE_HANDLER, new IdleStateHandler(provider.getReaderIdle(), 0, 0));
        }
        p.addLast(provider.getGroup(), LEN_DECODER_HANDLER, new LengthFieldBasedFrameDecoder(0xFFFFFF, 0, 4, 0, 4));
        p.addLast(provider.getGroup(), LEN_ENCODER_HANDLER, new LengthFieldPrepender(4));
        p.addLast(provider.getGroup(), CHUNKED_WRITE_HANDLER, new ChunkedWriteHandler());
        p.addLast(provider.getGroup(), STRING_ENCODER_HANDLER, new StringEncoder(StandardCharsets.UTF_8));
        p.addLast(provider.getGroup(), STRING_DECODER_HANDLER, new StringDecoder(StandardCharsets.UTF_8));
        p.addLast(provider.getGroup(), provider.getName(), provider.getHandler());
    }

    public static Class<? extends Channel> channelClass(EventLoopGroup group) {
        switch (group.next().getClass().getSimpleName()) {
            case "NioEventLoop":
                return NioSocketChannel.class;
            case "EpollEventLoop":
                return EpollSocketChannel.class;
            case "KQueueEventLoop":
                return KQueueSocketChannel.class;
            default:
                throw new IllegalArgumentException("unsupported loop");
        }
    }

    public static Class<? extends ServerSocketChannel> serverChannelClass(EventLoopGroup group) {
        switch (group.next().getClass().getSimpleName()) {
            case "NioEventLoop":
                return NioServerSocketChannel.class;
            case "EpollEventLoop":
                return EpollServerSocketChannel.class;
            case "KQueueEventLoop":
                return KQueueServerSocketChannel.class;
            default:
                throw new IllegalArgumentException("unsupported loop");
        }
    }

    public static EventLoopGroup newEventLoopGroup(int nThreads) {
        try {
            if (PlatformUtil.isMac()) {
                return new KQueueEventLoopGroup(nThreads);
            } else if (PlatformUtil.isLinux()) {
                return new EpollEventLoopGroup(nThreads);
            }
        } catch (Throwable e) {
            logger.warn("New event loop group failed, try to use NioEventLoopGroup, platform: {}, case:{}", PlatformUtil.arch(), e.getMessage());
        }
        return new NioEventLoopGroup(nThreads);
    }
}
