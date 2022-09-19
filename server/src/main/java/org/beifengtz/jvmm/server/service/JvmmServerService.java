package org.beifengtz.jvmm.server.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Promise;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.convey.channel.JvmmServerChannelInitializer;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.handler.JvmmServerHandlerProvider;
import org.slf4j.Logger;

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

    private static final Logger logger = LoggerFactory.logger(JvmmServerService.class);

    @Override
    protected Logger logger() {
        return logger;
    }

    @Override
    protected void startUp(Promise<Integer> promise) {
        EventLoopGroup workerGroup = getWorkableGlobalWorkerGroup();
        ChannelFuture future = new ServerBootstrap()
                .group(ServerContext.getBoosGroup(), workerGroup)
                .channel(ChannelInitializers.serverChannelClass(ServerContext.getBoosGroup()))
                .childHandler(new JvmmServerChannelInitializer(new JvmmServerHandlerProvider(10, workerGroup)))
                .bind(runningPort.get())
                .syncUninterruptibly();

        promise.trySuccess(runningPort.get());
        logger().info("Jvmm server service started on {}, node name: {}", runningPort.get(), ServerContext.getConfiguration().getName());

        future.channel().closeFuture().syncUninterruptibly();
    }

    @Override
    protected void onShutdown() {
        logger.info("Trigger to shutdown jvmm server");
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
