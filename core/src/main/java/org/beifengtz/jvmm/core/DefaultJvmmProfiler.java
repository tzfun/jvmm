package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.common.exception.ProfilerNotSupportedException;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerAction;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerCommander;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerCounter;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerEvent;
import org.beifengtz.jvmm.common.factory.ExecutorFactory;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
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

    private static final Logger log = LoggerFactory.logger(DefaultJvmmProfiler.class);

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
        return profiler.execute(command);
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
    public Future<String> sample(ScheduledExecutorService executor, File to, ProfilerEvent event, ProfilerCounter counter, long interval, long time, TimeUnit timeUnit) {
        if (to.getParentFile() != null && !to.getParentFile().exists()) {
            to.getParentFile().mkdirs();
        }

        execute(ProfilerCommander.newInstance()
                .setAction(ProfilerAction.start)
                .setEvent(event)
                .setInterval(interval)
                .setCounter(counter)
                .setAllKernel(true)
                .setAllUser(true)
                .setThreads(true));

        return executor.schedule(() -> execute(
                ProfilerCommander.newInstance().setAction(ProfilerAction.stop).setFile(to.getAbsolutePath())
        ), time, timeUnit);
    }

    @Override
    public Future<String> sample(File to, ProfilerEvent event, ProfilerCounter counter, long interval, long time, TimeUnit timeUnit) {
        return this.sample(getDefaultExecutor(), to, event, counter, interval, time, timeUnit);
    }

    @Override
    public Future<String> sample(ScheduledExecutorService executor, File to, ProfilerEvent event, ProfilerCounter counter, long time, TimeUnit timeUnit) {
        return this.sample(executor, to, event, counter, DEFAULT_INTERVAL, time, timeUnit);
    }

    @Override
    public Future<String> sample(File to, ProfilerEvent event, ProfilerCounter counter, long time, TimeUnit timeUnit) {
        return sample(to, event, counter, DEFAULT_INTERVAL, time, timeUnit);
    }

    @Override
    public Future<String> sample(ScheduledExecutorService executor, File to, ProfilerEvent event, long time, TimeUnit timeUnit) {
        return sample(executor, to, event, ProfilerCounter.samples, time, timeUnit);
    }

    @Override
    public Future<String> sample(File to, ProfilerEvent event, long time, TimeUnit timeUnit) {
        return sample(to, event, ProfilerCounter.samples, time, timeUnit);
    }

    @Override
    public Future<String> dumpCollapsed(ScheduledExecutorService executor, ProfilerCounter counter, ProfilerEvent event, long interval, long time, TimeUnit timeUnit) {
        execute(ProfilerCommander.newInstance()
                .setAction(ProfilerAction.start)
                .setEvent(event)
                .setInterval(interval)
                .setCounter(counter));

        return executor.schedule(() -> execute(
                ProfilerCommander.newInstance().setAction(ProfilerAction.collapsed).setCounter(counter)
        ), time, timeUnit);
    }

    @Override
    public Future<String> dumpCollapsed(ScheduledExecutorService executor, ProfilerCounter counter, ProfilerEvent event, long time, TimeUnit timeUnit) {
        return dumpCollapsed(executor, counter, event, DEFAULT_INTERVAL, time, timeUnit);
    }

    @Override
    public Future<String> dumpCollapsed(ProfilerCounter counter, ProfilerEvent event, long interval, long time, TimeUnit timeUnit) {
        return dumpCollapsed(getDefaultExecutor(), counter, event, interval, time, timeUnit);
    }

    @Override
    public Future<String> dumpCollapsed(ProfilerCounter counter, ProfilerEvent event, long time, TimeUnit timeUnit) {
        return dumpCollapsed(counter, event, DEFAULT_INTERVAL, time, timeUnit);
    }

    @Override
    public Future<String> dumpTraces(ScheduledExecutorService executor, int maxTraces, ProfilerEvent event, long interval, long time, TimeUnit timeUnit) {
        execute(ProfilerCommander.newInstance()
                .setAction(ProfilerAction.start)
                .setEvent(event)
                .setInterval(interval));

        return executor.schedule(() -> execute(
                ProfilerCommander.newInstance().setAction(ProfilerAction.summary).setTraces(maxTraces)
        ), time, timeUnit);
    }

    @Override
    public Future<String> dumpTraces(ScheduledExecutorService executor, int maxTraces, ProfilerEvent event, long time, TimeUnit timeUnit) {
        return dumpTraces(executor, maxTraces, event, DEFAULT_INTERVAL, time, timeUnit);
    }

    @Override
    public Future<String> dumpTraces(int maxTraces, ProfilerEvent event, long interval, long time, TimeUnit timeUnit) {
        return dumpTraces(getDefaultExecutor(), maxTraces, event, interval, time, timeUnit);
    }

    @Override
    public Future<String> dumpTraces(int maxTraces, ProfilerEvent event, long time, TimeUnit timeUnit) {
        return dumpTraces(maxTraces, event, DEFAULT_INTERVAL, time, timeUnit);
    }

    @Override
    public Future<String> dumpFlat(ScheduledExecutorService executor, int maxMethods, ProfilerEvent event, long interval, long time, TimeUnit timeUnit) {
        execute(ProfilerCommander.newInstance()
                .setAction(ProfilerAction.start)
                .setEvent(event)
                .setInterval(interval));

        return executor.schedule(() -> execute(
                ProfilerCommander.newInstance().setAction(ProfilerAction.summary).setFlat(maxMethods)
        ), time, timeUnit);
    }

    @Override
    public Future<String> dumpFlat(ScheduledExecutorService executor, int maxMethods, ProfilerEvent event, long time, TimeUnit timeUnit) {
        return dumpFlat(executor, maxMethods, event, DEFAULT_INTERVAL, time, timeUnit);
    }

    @Override
    public Future<String> dumpFlat(int maxMethods, ProfilerEvent event, long interval, long time, TimeUnit timeUnit) {
        return dumpFlat(getDefaultExecutor(), maxMethods, event, interval, time, timeUnit);
    }

    @Override
    public Future<String> dumpFlat(int maxMethods, ProfilerEvent event, long time, TimeUnit timeUnit) {
        return dumpFlat(getDefaultExecutor(), maxMethods, event, DEFAULT_INTERVAL, time, timeUnit);
    }
}
