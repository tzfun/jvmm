package org.beifengtz.jvmm.server.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Promise;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.convey.channel.HttpServerChannelInitializer;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.entity.conf.HttpServerConf;
import org.beifengtz.jvmm.server.handler.HttpServerHandlerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 09:46 2022/9/7
 *
 * @author beifengtz
 */
public class JvmmHttpServerService extends AbstractListenerServerService {

    private static final Logger logger = LoggerFactory.getLogger(JvmmHttpServerService.class);

    protected Channel channel;

    @Override
    protected HttpServerConf getConf() {
        return ServerContext.getConfiguration().getServer().getHttp();
    }

    @Override
    protected Logger logger() {
        return logger;
    }

    @Override
    protected void startUp(Promise<Integer> promise) {
        EventLoopGroup group = ServerContext.getWorkerGroup();
        ChannelFuture future = new ServerBootstrap()
                .group(group, group)
                .channel(ChannelInitializers.serverChannelClass(ServerContext.getWorkerGroup()))
                .childHandler(new HttpServerChannelInitializer(new HttpServerHandlerProvider(10, group)))
                .bind(runningPort.get())
                .syncUninterruptibly();

        promise.trySuccess(runningPort.get());
        logger().info("Http server service started on {}, node name: {}", runningPort.get(), ServerContext.getConfiguration().getName());
        channel = future.channel();
        channel.closeFuture().syncUninterruptibly();
    }

    @Override
    protected void onShutdown() {
        logger.info("Trigger to shutdown http server...");
        if (channel != null) {
            channel.close();
        }
    }

    @Override
    public int hashCode() {
        return 2;
    }
}
