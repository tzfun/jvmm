package org.beifengtz.jvmm.demo.subscriber;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.EventExecutorGroup;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.convey.channel.HttpServerChannelInitializer;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.handler.HttpServerHandlerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description TODO
 * date 23:20 2023/6/22
 *
 * @author beifengtz
 */
public class SubscriberDemo {

    private static final Logger logger = LoggerFactory.getLogger(SubscriberDemo.class);
    public static void main(String[] args) {
        int port = 8081;
        EventLoopGroup group = ServerContext.getWorkerGroup();
        ChannelFuture future = new ServerBootstrap()
                .group(group)
                .channel(ChannelInitializers.serverChannelClass(group))
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
