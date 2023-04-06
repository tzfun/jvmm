package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.common.exception.ProfilerNotSupportedException;
import org.beifengtz.jvmm.common.factory.ExecutorFactory;
import org.beifengtz.jvmm.common.util.meta.ListenableFuture;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerAction;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerCommander;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 9:42 下午 2021/6/26
 *
 * @author beifengtz
 */
class DefaultJvmmProfiler implements JvmmProfiler {

    private static final Logger log = LoggerFactory.getLogger(DefaultJvmmProfiler.class);

    /**
     * 默认采样间隔，单位纳秒ns
     */
    private static final int DEFAULT_INTERVAL = 10000000;

    private static AsyncProfiler profiler;

    static {
        try {
            profiler = AsyncProfiler.getInstance();
        } catch (IllegalStateException e) {
            profiler = null;
            log.warn(e.getMessage());
            log.debug(e.getMessage(), e);
        }
    }

    DefaultJvmmProfiler() {
    }

    private ScheduledExecutorService getDefaultExecutor() {
        return ExecutorFactory.getScheduleThreadPool();
    }

    @Override
    public boolean isSystemSupported() {
        return profiler != null;
    }

    @Override
    public String execute(String command) throws IOException {
        if (!isSystemSupported()) {
            throw new ProfilerNotSupportedException("System not supported async-profiler");
        }
        log.info("Execute profiler command: " + command);
        try {
            return profiler.execute(command);
        } catch (IOException e) {
            profiler.stop();
            throw e;
        }
    }

    @Override
    public String execute(ProfilerCommander commander) {
        try {
            return execute(commander.build());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String enabledEvents() {
        return execute(ProfilerCommander.newInstance().setAction(ProfilerAction.list));
    }

    @Override
    public String status() {
        return execute(ProfilerCommander.newInstance().setAction(ProfilerAction.status));
    }

    @Override
    public String version() {
        return execute(ProfilerCommander.newInstance().setAction(ProfilerAction.version));
    }

    @Override
    public String start(String event, ProfilerCounter counter, long interval) {
        return execute(ProfilerCommander.newInstance()
                .setAction(ProfilerAction.start)
                .setEvent(event)
                .setInterval(interval <= 1000 ? DEFAULT_INTERVAL : interval)
                .setCounter(counter)
                .setAllKernel(true)
                .setAllUser(true)
                .setThreads(true));
    }

    @Override
    public String stop(File to) {
        return execute(ProfilerCommander.newInstance()
                .setAction(ProfilerAction.stop)
                .setFile(to.getAbsolutePath()));
    }

    @Override
    public ListenableFuture<String> sample(ScheduledExecutorService executor, File to, String event, ProfilerCounter counter, long interval, long time, TimeUnit timeUnit) {
        int dotIdx = to.getName().lastIndexOf(".");
        if (dotIdx >= 0) {
            String format = to.getName().substring(dotIdx + 1);
            if ("csv".equalsIgnoreCase(format)) {
                throw new IllegalArgumentException("SVG format is obsolete, use .html for FlameGraph");
            } else if (!format.toLowerCase().matches("(txt|html|jfr)")) {
                throw new IllegalArgumentException("Invalid flame graph format: " + format + ", expected: txt, html, jfr");
            }
        }

        start(event, counter, interval);
        ListenableFuture<String> future = new ListenableFuture<>();
        executor.schedule(() -> {
            try {
                future.complete(stop(to));
            } catch (Throwable t) {
                future.cause(t);
            }
        }, time <= 0 ? 10 : time, timeUnit);
        return future;
    }

    @Override
    public ListenableFuture<String> sample(File to, String event, ProfilerCounter counter, long interval, long time, TimeUnit timeUnit) {
        return this.sample(getDefaultExecutor(), to, event, counter, interval, time, timeUnit);
    }

    @Override
    public ListenableFuture<String> sample(ScheduledExecutorService executor, File to, String event, ProfilerCounter counter, long time, TimeUnit timeUnit) {
        return this.sample(executor, to, event, counter, DEFAULT_INTERVAL, time, timeUnit);
    }

    @Override
    public ListenableFuture<String> sample(File to, String event, ProfilerCounter counter, long time, TimeUnit timeUnit) {
        return sample(to, event, counter, DEFAULT_INTERVAL, time, timeUnit);
    }

    @Override
    public ListenableFuture<String> sample(ScheduledExecutorService executor, File to, String event, long time, TimeUnit timeUnit) {
        return sample(executor, to, event, ProfilerCounter.samples, time, timeUnit);
    }

    @Override
    public ListenableFuture<String> sample(File to, String event, long time, TimeUnit timeUnit) {
        return sample(to, event, ProfilerCounter.samples, time, timeUnit);
    }

    @Override
    public ListenableFuture<String> dumpCollapsed(ScheduledExecutorService executor, ProfilerCounter counter, String event, long interval, long time, TimeUnit timeUnit) {
        execute(ProfilerCommander.newInstance()
                .setAction(ProfilerAction.start)
                .setEvent(event)
                .setInterval(interval)
                .setCounter(counter));
        ListenableFuture<String> future = new ListenableFuture<>();

        executor.schedule(() -> {
            try {
                future.complete(execute(
                        ProfilerCommander.newInstance().setAction(ProfilerAction.collapsed).setCounter(counter)
                ));
            } catch (Throwable t) {
                future.cause(t);
            }
        }, time, timeUnit);

        return future;
    }

    @Override
    public ListenableFuture<String> dumpCollapsed(ScheduledExecutorService executor, ProfilerCounter counter, String event, long time, TimeUnit timeUnit) {
        return dumpCollapsed(executor, counter, event, DEFAULT_INTERVAL, time, timeUnit);
    }

    @Override
    public ListenableFuture<String> dumpCollapsed(ProfilerCounter counter, String event, long interval, long time, TimeUnit timeUnit) {
        return dumpCollapsed(getDefaultExecutor(), counter, event, interval, time, timeUnit);
    }

    @Override
    public ListenableFuture<String> dumpCollapsed(ProfilerCounter counter, String event, long time, TimeUnit timeUnit) {
        return dumpCollapsed(counter, event, DEFAULT_INTERVAL, time, timeUnit);
    }

    @Override
    public ListenableFuture<String> dumpTraces(ScheduledExecutorService executor, int maxTraces, String event, long interval, long time, TimeUnit timeUnit) {
        execute(ProfilerCommander.newInstance()
                .setAction(ProfilerAction.start)
                .setEvent(event)
                .setInterval(interval));
        ListenableFuture<String> future = new ListenableFuture<>();
        executor.schedule(() -> {
            try {
                future.complete(execute(
                        ProfilerCommander.newInstance().setAction(ProfilerAction.summary).setTraces(maxTraces)
                ));
            } catch (Throwable t) {
                future.cause(t);
            }
        }, time, timeUnit);
        return future;
    }

    @Override
    public ListenableFuture<String> dumpTraces(ScheduledExecutorService executor, int maxTraces, String event, long time, TimeUnit timeUnit) {
        return dumpTraces(executor, maxTraces, event, DEFAULT_INTERVAL, time, timeUnit);
    }

    @Override
    public ListenableFuture<String> dumpTraces(int maxTraces, String event, long interval, long time, TimeUnit timeUnit) {
        return dumpTraces(getDefaultExecutor(), maxTraces, event, interval, time, timeUnit);
    }

    @Override
    public ListenableFuture<String> dumpTraces(int maxTraces, String event, long time, TimeUnit timeUnit) {
        return dumpTraces(maxTraces, event, DEFAULT_INTERVAL, time, timeUnit);
    }

    @Override
    public ListenableFuture<String> dumpFlat(ScheduledExecutorService executor, int maxMethods, String event, long interval, long time, TimeUnit timeUnit) {
        execute(ProfilerCommander.newInstance()
                .setAction(ProfilerAction.start)
                .setEvent(event)
                .setInterval(interval));

        ListenableFuture<String> future = new ListenableFuture<>();
        executor.schedule(() -> {
            try {
                future.complete(execute(
                        ProfilerCommander.newInstance().setAction(ProfilerAction.summary).setFlat(maxMethods)
                ));
            } catch (Throwable t) {
                future.cause(t);
            }
        }, time, timeUnit);
        return future;
    }

    @Override
    public ListenableFuture<String> dumpFlat(ScheduledExecutorService executor, int maxMethods, String event, long time, TimeUnit timeUnit) {
        return dumpFlat(executor, maxMethods, event, DEFAULT_INTERVAL, time, timeUnit);
    }

    @Override
    public ListenableFuture<String> dumpFlat(int maxMethods, String event, long interval, long time, TimeUnit timeUnit) {
        return dumpFlat(getDefaultExecutor(), maxMethods, event, interval, time, timeUnit);
    }

    @Override
    public ListenableFuture<String> dumpFlat(int maxMethods, String event, long time, TimeUnit timeUnit) {
        return dumpFlat(getDefaultExecutor(), maxMethods, event, DEFAULT_INTERVAL, time, timeUnit);
    }
}
