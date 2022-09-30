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
public abstract class OsScheduledService implements OsProvider, Runnable {

    protected static final Logger logger = LoggerFactory.logger(OsScheduledService.class);
    protected static final int DEFAULT_INTERVAL_SECS = 3;
    protected static final int MIN_SCHEDULE_SECS = 1;
    protected static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    protected final AtomicBoolean running = new AtomicBoolean(false);
    protected ScheduledFuture<?> scheduledFuture;
    private int interval;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
    }

    public int getIntervalSecs() {
        return interval < MIN_SCHEDULE_SECS ? DEFAULT_INTERVAL_SECS : interval;
    }

    /**
     * 设置定期执行间隔，单位秒
     */
    public void setIntervalSecs(int secs) {
        interval = secs;
    }

    /**
     * 启动service
     */
    @Override
    public void start() {
        running.set(true);
        scheduledFuture = executor.scheduleWithFixedDelay(this, 0, getIntervalSecs(), TimeUnit.SECONDS);
    }

    /**
     * 停止运行
     */
    @Override
    public void shutdown() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            running.set(false);
            logger.info("Os schedule service stopped, target: {}", getClass().getSimpleName());
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

}
