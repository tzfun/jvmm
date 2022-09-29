package org.beifengtz.jvmm.core.ext.os;

import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description: TODO
 *
 * Created in 14:46 2022/9/29
 *
 * @author beifengtz
 */
public abstract class OsScheduledService implements Runnable {

    protected static final Logger logger = LoggerFactory.logger(OsScheduledService.class);
    protected volatile ScheduledExecutorService executor = null;
    protected static final int DEFAULT_SCHEDULE_SECS = 3;
    protected static final int MIN_SCHEDULE_SECS = 1;
    protected final AtomicBoolean runnable = new AtomicBoolean(false);
    protected ScheduledFuture<?> scheduledFuture;

    protected int scheduleSecs() {
        return DEFAULT_SCHEDULE_SECS;
    }

    /**
     * 启动service
     */
    public void start() {
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newScheduledThreadPool(1);
        }
        runnable.set(true);
        OsScheduledService that = this;
        int s = scheduleSecs();
        scheduledFuture = executor.scheduleWithFixedDelay(that, 0, s < MIN_SCHEDULE_SECS ? DEFAULT_SCHEDULE_SECS : s, TimeUnit.SECONDS);
    }

    /**
     * 停止运行
     */
    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            logger.info("Os schedule service stopped, target: {}", getClass().getSimpleName());
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

}
