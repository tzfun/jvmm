package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.common.exception.ProfilerNotSupportedException;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerAction;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerCommander;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerCounter;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerEvent;
import org.beifengtz.jvmm.tools.factory.ExecutorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
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
public class DefaultJvmmProfiler implements JvmmProfiler {

    private static final Logger log = LoggerFactory.getLogger(DefaultJvmmProfiler.class);

    private static AsyncProfiler profiler;

    static {
        try {
            profiler = AsyncProfiler.getInstance();
        } catch (IllegalStateException e) {
            profiler = null;
            log.warn(e.getMessage(), e);
        }
    }

    DefaultJvmmProfiler() {
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
    public Future<?> sample(File to, ProfilerEvent event, ProfilerCounter counter, long interval, long time, TimeUnit timeUnit) {
        if (to.getParentFile() != null && !to.getParentFile().exists()) {
            to.getParentFile().mkdirs();
        }

        execute(ProfilerCommander.newInstance()
                .setAction(ProfilerAction.start)
                .setEvent(event)
                .setCounter(counter)
                .setAllKernel(true)
                .setAllUser(true)
                .setThreads(true));

        return ExecutorFactory.getScheduleThreadPool().schedule(() -> execute(ProfilerCommander.newInstance()
                .setAction(ProfilerAction.stop).setFile(to.getAbsolutePath())
        ), time, timeUnit);
    }

    @Override
    public Future<?> sample(File to, ProfilerEvent event, ProfilerCounter counter, long time, TimeUnit timeUnit) {
        return sample(to, event, counter, 10000000, time, timeUnit);
    }

    @Override
    public Future<?> sample(File to, ProfilerEvent event, long time, TimeUnit timeUnit) {
        return sample(to, event, ProfilerCounter.samples, time, timeUnit);
    }
}
