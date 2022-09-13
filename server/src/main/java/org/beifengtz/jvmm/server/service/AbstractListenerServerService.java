package org.beifengtz.jvmm.server.service;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Promise;
import org.beifengtz.jvmm.common.util.PlatformUtil;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.core.conf.entity.JvmmServerConf;
import org.beifengtz.jvmm.server.ServerContext;
import org.slf4j.Logger;

import java.net.BindException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:03 2022/9/13
 *
 * @author beifengtz
 */
public abstract class AbstractListenerServerService implements JvmmService {

    protected int runningPort;
    protected final AtomicInteger retry = new AtomicInteger(0);
    private static EventLoopGroup globalWorkerGroup;

    protected static EventLoopGroup getWorkableGlobalWorkerGroup() {
        if (globalWorkerGroup == null || globalWorkerGroup.isShutdown()) {
            synchronized (AbstractListenerServerService.class) {
                if (globalWorkerGroup == null || globalWorkerGroup.isShutdown()) {
                    globalWorkerGroup = ChannelInitializers.newEventLoopGroup(ServerContext.getConfiguration().getWorkThread());
                }
            }
        }
        return globalWorkerGroup;
    }

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
            logger().info("Try to start jvmm server service. target port: {}", runningPort);

            try {
                if (retry.get() > BIND_LIMIT_TIMES) {
                    throw new BindException("The number of port monitoring retries exceeds the limit: " + BIND_LIMIT_TIMES);
                }

                startUp();

            } catch (BindException e) {
                if (retry.get() <= BIND_LIMIT_TIMES && conf.isAdaptivePort()) {
                    runningPort++;
                    start(promise);
                } else {
                    logger().error("Jvmm server start up failed. " + e.getMessage(), e);
                    promise.tryFailure(e);
                    stop();
                }
            } catch (Throwable e) {
                logger().error("Jvmm server start up failed. " + e.getMessage(), e);
                promise.tryFailure(e);
                stop();
            }
        } else {
            logger().info("Port {} is not available, auto increase:{}", runningPort, conf.isAdaptivePort());
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
        shutdown();
        if (globalWorkerGroup != null) {
            globalWorkerGroup.shutdownGracefully();
        }
        runningPort = -1;
        retry.set(0);
    }

    @Override
    public int getPort() {
        return runningPort;
    }

    protected abstract Logger logger();

    protected abstract void startUp();

    protected abstract void shutdown();
}
