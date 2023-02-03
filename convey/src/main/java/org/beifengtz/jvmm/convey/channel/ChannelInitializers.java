package org.beifengtz.jvmm.convey.channel;

import io.netty.channel.Channel;
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
import org.beifengtz.jvmm.common.util.PlatformUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:21 2022/9/7
 *
 * @author beifengtz
 */
public class ChannelInitializers {
    private static final Logger logger = LoggerFactory.getLogger(ChannelInitializers.class);

    public static final String IDLE_STATE_HANDLER = "idleStateHandler";

    public static final String LEN_DECODER_HANDLER = "lenDecoderHandler";
    public static final String LEN_ENCODER_HANDLER = "lenEncoderHandler";
    public static final String STRING_DECODER_HANDLER = "stringDecoderHandler";
    public static final String STRING_ENCODER_HANDLER = "stringEncoderHandler";
    public static final String CHUNKED_WRITE_HANDLER = "chunkedWriteHandler";
    public static final String HTTP_CODEC_HANDLER = "httpCodecHandler";
    public static final String AGGREGATOR_HANDLER = "HttpObjectAggregator";

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
        return newEventLoopGroup(nThreads, null);
    }

    public static EventLoopGroup newEventLoopGroup(int nThreads, Executor executor) {
        try {
            if (PlatformUtil.isMac()) {
                return new KQueueEventLoopGroup(nThreads, executor);
            } else if (PlatformUtil.isLinux()) {
                return new EpollEventLoopGroup(nThreads, executor);
            }
        } catch (Throwable e) {
            logger.debug("New event loop group failed, try to use NioEventLoopGroup, platform: {}, case: {}", PlatformUtil.arch(), e.getMessage());
        }
        return new NioEventLoopGroup(nThreads, executor);
    }
}
