package org.beifengtz.jvmm.common.factory;

import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.common.util.SystemPropertyUtil;
import org.slf4j.LoggerFactory;

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

    private static volatile ScheduledExecutorService SCHEDULE_THREAD_POOL;

    static {
        Thread shutdownHook = new Thread(ExecutorFactory::shutdown);
        shutdownHook.setName("jvmmExtHook");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private static ThreadFactory getThreadFactory(String name) {
        return new DefaultThreadFactory(StringUtil.isEmpty(name) ? "jvmm" : name);
    }

    public static int getNThreads() {
        return SystemPropertyUtil.getInt("jvmm.workThread", 2 * Runtime.getRuntime().availableProcessors());
    }

    public static ScheduledExecutorService getScheduleThreadPool() {
        if (SCHEDULE_THREAD_POOL == null || SCHEDULE_THREAD_POOL.isShutdown()) {
            synchronized (ExecutorFactory.class) {
                if (SCHEDULE_THREAD_POOL == null || SCHEDULE_THREAD_POOL.isShutdown()) {
                    SCHEDULE_THREAD_POOL = Executors.newScheduledThreadPool(getNThreads(), getThreadFactory("jvmm-schedule"));
                }
            }
        }
        return SCHEDULE_THREAD_POOL;
    }

    public static void shutdown() {
        if (SCHEDULE_THREAD_POOL != null && !SCHEDULE_THREAD_POOL.isShutdown()) {
            SCHEDULE_THREAD_POOL.shutdown();
            if (SCHEDULE_THREAD_POOL.isShutdown()) {
                LoggerFactory.getLogger(ExecutorFactory.class).info("jvmm schedule thread pool shutdown.");
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
