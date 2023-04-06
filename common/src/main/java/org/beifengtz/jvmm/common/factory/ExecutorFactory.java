package org.beifengtz.jvmm.common.factory;

import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.common.util.SystemPropertyUtil;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
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
        shutdownHook.setName("jvmm-shutdown-hook");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private static ThreadFactory getThreadFactory(String name) {
        return new DefaultThreadFactory(StringUtil.isEmpty(name) ? "jvmm" : name);
    }

    public static int getNThreads() {
        return Math.max(2, SystemPropertyUtil.getInt("jvmm.workThread", Runtime.getRuntime().availableProcessors()));
    }

    public static ScheduledExecutorService getScheduleThreadPool() {
        if (SCHEDULE_THREAD_POOL == null) {
            synchronized (ExecutorFactory.class) {
                if (SCHEDULE_THREAD_POOL == null) {
                    SCHEDULE_THREAD_POOL = new ScheduledThreadPoolExecutor(getNThreads(), getThreadFactory("jvmm"), new CallerRunsPolicy());
                }
            }
        }
        return SCHEDULE_THREAD_POOL;
    }

    private static void shutdown() {
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
            if (t.isDaemon()) {
                t.setDaemon(false);
            }

            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }

            return t;
        }
    }
}
