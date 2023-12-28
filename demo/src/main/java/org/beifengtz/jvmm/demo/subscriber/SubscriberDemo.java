package org.beifengtz.jvmm.demo.subscriber;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.convey.channel.ChannelUtil;
import org.beifengtz.jvmm.convey.channel.HttpServerChannelInitializer;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;
import org.beifengtz.jvmm.server.ServerContext;

/**
 * description TODO
 * date 23:20 2023/6/22
 *
 * @author beifengtz
 */
public class SubscriberDemo {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SubscriberDemo.class);

    public static void main(String[] args) {
        int port = 8081;
        EventLoopGroup group = ServerContext.getWorkerGroup();
        ChannelFuture future = new ServerBootstrap()
                .group(group)
                .channel(ChannelUtil.serverChannelClass(group))
                .childHandler(new HttpServerChannelInitializer(new HandlerProvider() {
                    @Override
                    public ChannelHandler getHandler() {
                        return new SubscriberHandler();
                    }

                    @Override
                    public EventExecutorGroup getGroup() {
                        return group;
                    }
                }))
                .bind(port)
                .syncUninterruptibly();
        logger.info("Subscriber demo boot on {} successfully.", port);
        future.channel().closeFuture().syncUninterruptibly();
    }
}
