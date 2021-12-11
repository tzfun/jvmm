package org.beifengtz.jvmm.common.factory;

import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.common.util.SystemPropertyUtil;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:23 2021/5/11
 *
 * @author beifengtz
 */
public class ExecutorFactory {

    private static final Logger log = LoggerFactory.logger(ExecutorFactory.class);

    private static volatile ScheduledExecutorService SCHEDULE_THREAD_POOL;
    private static volatile ExecutorService FIXED_THREAD_POOL;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(ExecutorFactory::shutdown));
    }

    private static ThreadFactory getThreadFactory(String name) {
        return new DefaultThreadFactory(StringUtil.isEmpty(name) ? "jvmm" : name);
    }

    private static int getProcessors() {
        return SystemPropertyUtil.getInt("jvmm.workThread", Runtime.getRuntime().availableProcessors());
    }

    public static ScheduledExecutorService getScheduleThreadPool() {
        if (SCHEDULE_THREAD_POOL == null || SCHEDULE_THREAD_POOL.isShutdown()) {
            synchronized (ExecutorFactory.class) {
                if (SCHEDULE_THREAD_POOL == null || SCHEDULE_THREAD_POOL.isShutdown()) {
                    SCHEDULE_THREAD_POOL = Executors.newSingleThreadScheduledExecutor(getThreadFactory("jvmm-schedule"));
                }
            }
        }
        return SCHEDULE_THREAD_POOL;
    }

    public static ExecutorService getMultiThreadPool() {
        if (FIXED_THREAD_POOL == null || FIXED_THREAD_POOL.isShutdown()) {
            synchronized (ExecutorFactory.class) {
                if (FIXED_THREAD_POOL == null || FIXED_THREAD_POOL.isShutdown()) {
                    FIXED_THREAD_POOL = Executors.newFixedThreadPool(getProcessors(), getThreadFactory("jvmm-fixed"));
                }
            }
        }
        return FIXED_THREAD_POOL;
    }

    public static void shutdown() {
        if (SCHEDULE_THREAD_POOL != null) {
            SCHEDULE_THREAD_POOL.shutdown();
            if (SCHEDULE_THREAD_POOL.isShutdown()) {
                log.info("jvmm schedule thread pool shutdown.");
            }
        }

        if (FIXED_THREAD_POOL != null) {
            FIXED_THREAD_POOL.shutdown();
            if (FIXED_THREAD_POOL.isShutdown()) {
                log.info("jvmm multi thread pool shutdown.");
            }
        }
    }

    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory(String name) {
            group = new ThreadGroup("group-" + name);
            namePrefix = name + "-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
