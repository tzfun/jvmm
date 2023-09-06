package org.beifengtz.jvmm.server.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.convey.channel.JvmmServerChannelInitializer;
import org.beifengtz.jvmm.convey.handler.JvmmChannelHandler;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.entity.conf.JvmmServerConf;
import org.beifengtz.jvmm.server.handler.JvmmServerHandlerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 09:49 2022/9/7
 *
 * @author beifengtz
 */
public class JvmmServerService extends AbstractListenerServerService {

    private static final Logger logger = LoggerFactory.getLogger(JvmmServerService.class);

    protected volatile Channel channel;

    @Override
    protected JvmmServerConf getConf() {
        return ServerContext.getConfiguration().getServer().getJvmm();
    }

    @Override
    protected Logger logger() {
        return logger;
    }

    @Override
    protected void startUp(Promise<Integer> promise) {
        JvmmChannelHandler.init();
        EventLoopGroup group = ServerContext.getWorkerGroup();
        ChannelFuture future = new ServerBootstrap()
                .group(group)
                .channel(ChannelInitializers.serverChannelClass(ServerContext.getWorkerGroup()))
                .childHandler(new JvmmServerChannelInitializer(new JvmmServerHandlerProvider(10, group)))
                .bind(runningPort.get())
                .syncUninterruptibly();

        promise.trySuccess(runningPort.get());
        logger().info("Jvmm server service started on {}, node name: {}", runningPort.get(), ServerContext.getConfiguration().getName());
        channel = future.channel();
    }

    @Override
    protected void onShutdown() {
        if (channel != null) {
            logger.info("Trigger to shutdown jvmm server...");
            channel.close().addListener((GenericFutureListener<Future<Void>>) future -> {
                if (future.isSuccess()) {
                    logger.info("Jvmm server is shutdown");
                    JvmmChannelHandler.closeAllChannels().addListener((GenericFutureListener<Future<Void>>) f -> {
                        logger.info("Jvmm server all channels has closed");
                    });
                }
            });
        }
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
