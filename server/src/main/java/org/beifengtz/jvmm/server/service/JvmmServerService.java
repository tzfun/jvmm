package org.beifengtz.jvmm.server.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Promise;
import org.beifengtz.jvmm.common.util.PlatformUtil;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.convey.channel.JvmmServerChannelInitializer;
import org.beifengtz.jvmm.core.conf.entity.JvmmServerConf;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.handler.JvmmServerHandlerProvider;
import org.slf4j.Logger;

import java.net.BindException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.beifengtz.jvmm.common.factory.LoggerFactory.logger;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 09:49 2022/9/7
 *
 * @author beifengtz
 */
public class JvmmServerService implements JvmmService {
    private static final Logger logger = logger(JvmmServerService.class);
    private int runningPort;
    private final AtomicInteger retry = new AtomicInteger(0);
    private static EventLoopGroup workerGroup;

    @Override
    public void start(Promise<Integer> promise) {
        JvmmServerConf conf = ServerContext.getConfiguration().getServer().getJvmm();
        if (conf == null) {
            promise.tryFailure(new IllegalArgumentException("No jvmm configuration"));
            return;
        }
        runningPort = conf.getPort();
        retry.incrementAndGet();

        if (PlatformUtil.portAvailable(runningPort)) {
            logger.info("Try to start jvmm server service. target port: {}", runningPort);
            final ServerBootstrap b = new ServerBootstrap();

            try {
                if (retry.get() > BIND_LIMIT_TIMES) {
                    throw new BindException("The number of port monitoring retries exceeds the limit: " + BIND_LIMIT_TIMES);
                }
                if (workerGroup == null || workerGroup.isShutdown()) {
                    synchronized (JvmmServerService.class) {
                        if (workerGroup == null || workerGroup.isShutdown()) {
                            workerGroup = ChannelInitializers.newEventLoopGroup(ServerContext.getConfiguration().getWorkThread());
                        }
                    }
                }

                ChannelFuture f = b.group(ServerContext.getBoosGroup(), workerGroup)
                        .channel(ChannelInitializers.serverChannelClass(ServerContext.getBoosGroup()))
                        .childHandler(new JvmmServerChannelInitializer(new JvmmServerHandlerProvider(10, workerGroup)))
                        .bind(runningPort).syncUninterruptibly();

                logger.info("Jvmm server service started on {}, node name: {}", runningPort, ServerContext.getConfiguration().getName());
                f.channel().closeFuture().syncUninterruptibly();
            } catch (BindException e) {
                if (retry.get() <= BIND_LIMIT_TIMES && conf.isAdaptivePort()) {
                    runningPort++;
                    start(promise);
                } else {
                    logger.error("Jvmm server start up failed. " + e.getMessage(), e);
                    promise.tryFailure(e);
                    stop();
                }
            } catch (Throwable e) {
                logger.error("Jvmm server start up failed. " + e.getMessage(), e);
                promise.tryFailure(e);
                stop();
            }
        } else {
            logger.info("Port {} is not available, auto increase:{}", runningPort, conf.isAdaptivePort());
            if (conf.isAdaptivePort()) {
                runningPort++;
                start(promise);
            } else {
                promise.tryFailure(new RuntimeException("Port " + runningPort + " is not available and the auto increase switch is closed."));
                stop();
            }
        }
    }

    @Override
    public void stop() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        runningPort = -1;
        retry.set(0);
    }

    @Override
    public int getPort() {
        return runningPort;
    }
}
