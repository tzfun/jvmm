package org.beifengtz.jvmm.server.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.convey.channel.JvmmServerChannelInitializer;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.handler.HttpServerHandlerProvider;
import org.slf4j.Logger;

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

    @Override
    protected Logger logger() {
        return LoggerFactory.logger(JvmmHttpServerService.class);
    }

    @Override
    protected void startUp() {
        EventLoopGroup workerGroup = getWorkableGlobalWorkerGroup();
        new ServerBootstrap()
                .group(ServerContext.getBoosGroup(), workerGroup)
                .channel(ChannelInitializers.serverChannelClass(ServerContext.getBoosGroup()))
                .childHandler(new JvmmServerChannelInitializer(new HttpServerHandlerProvider(10, workerGroup)))
                .bind(runningPort)
                .syncUninterruptibly()
                .channel()
                .closeFuture()
                .syncUninterruptibly();
        logger().info("Jvmm Http server service started on {}, node name: {}", runningPort, ServerContext.getConfiguration().getName());
    }

    @Override
    protected void shutdown() {

    }
}
