package org.beifengtz.jvmm.server.service;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.unix.Errors;
import io.netty.util.concurrent.Promise;
import org.beifengtz.jvmm.common.util.PlatformUtil;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.entity.conf.JvmmServerConf;
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

    protected AtomicInteger runningPort = new AtomicInteger();
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
        runningPort.set(conf.getPort());
        execute(conf, promise);
    }

    protected void execute(JvmmServerConf conf, Promise<Integer> promise) {
        retry.incrementAndGet();
        if (PlatformUtil.portAvailable(runningPort.get())) {
            try {
                if (retry.get() > BIND_LIMIT_TIMES) {
                    throw new BindException("The number of port monitoring retries exceeds the limit: " + BIND_LIMIT_TIMES);
                }

                startUp(promise);
            } catch (Errors.NativeIoException | BindException e) {
                if (retry.get() <= BIND_LIMIT_TIMES && conf.isAdaptivePort()) {
                    logger().warn("Port {} is not available, trying to find available ports by auto-incrementing ports.", runningPort.get());
                    runningPort.incrementAndGet();
                    execute(conf, promise);
                } else {
                    logger().error("Jvmm service start up failed." + e.getMessage(), e);
                    promise.tryFailure(e);
                    stop();
                }
            } catch (Throwable e) {
                logger().error("Jvmm service start up failed. " + e.getMessage(), e);
                promise.tryFailure(e);
                stop();
            }
        } else {
            if (conf.isAdaptivePort()) {
                logger().warn("Port {} is not available, trying to find available ports by auto-incrementing ports.", runningPort.get());
                runningPort.incrementAndGet();
                execute(conf, promise);
            } else {
                promise.tryFailure(new RuntimeException("Port " + runningPort.get() + " is not available and the auto increase switch is closed."));
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
        runningPort.set(-1);
        retry.set(0);
    }

    @Override
    public int getPort() {
        return runningPort.get();
    }

    protected abstract Logger logger();

    protected abstract void startUp(Promise<Integer> promise) throws Exception;

    protected abstract void shutdown();
}
