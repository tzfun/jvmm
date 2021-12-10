package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.tools.factory.ExecutorFactory;
import org.beifengtz.jvmm.tools.factory.LoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 17:18 2021/5/11
 *
 * @author beifengtz
 */
public class DefaultJvmmScheduleService implements JvmmScheduleService {

    private static final Logger log = LoggerFactory.logger(DefaultJvmmScheduleService.class);

    private final ScheduledExecutorService executor;
    private final AtomicReference<Runnable> task = new AtomicReference<>();
    private final AtomicReference<Thread.State> state;
    private final AtomicBoolean stopFlag = new AtomicBoolean(false);
    private final AtomicBoolean stopOnError = new AtomicBoolean(true);
    private final AtomicInteger timeGap = new AtomicInteger(10);
    private final String taskName;
    private final AtomicInteger targetTimes = new AtomicInteger(-1);
    private final AtomicInteger timesCounter = new AtomicInteger(0);

    {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public DefaultJvmmScheduleService(String taskName) {
        this(taskName, ExecutorFactory.getScheduleThreadPool());
    }

    public DefaultJvmmScheduleService(String taskName, ScheduledExecutorService executor) {
        this.taskName = taskName;
        this.executor = executor;
        this.state = new AtomicReference<>(Thread.State.NEW);
    }

    @Override
    public JvmmScheduleService setTask(Runnable task) {
        this.task.set(task);
        return this;
    }

    @Override
    public JvmmScheduleService setTimes(int times) {
        this.targetTimes.set(times);
        return this;
    }

    @Override
    public JvmmScheduleService setTimeGap(int gapSeconds) {
        if (gapSeconds > 0) {
            this.timeGap.set(gapSeconds);
        }
        return this;
    }

    @Override
    public JvmmScheduleService setStopOnError(boolean stopOnError) {
        this.stopOnError.set(stopOnError);
        return this;
    }

    @Override
    public void start() {
        if (state.get() == Thread.State.RUNNABLE) {
            return;
        }
        state.set(Thread.State.RUNNABLE);
        timesCounter.set(0);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    if (task.get() != null) {
                        task.get().run();
                        timesCounter.incrementAndGet();
                    }
                } catch (Exception e) {
                    log.error("Execute schedule task error. [" + taskName + "]", e);
                    if (stopOnError.get()) {
                        state.set(Thread.State.TERMINATED);
                        return;
                    }
                }

                if (!stopFlag.get() || (targetTimes.get() > 0 && timesCounter.get() > targetTimes.get())) {
                    executor.schedule(this, timeGap.get(), TimeUnit.SECONDS);
                } else {
                    log.info("Schedule task stopped. [" + taskName + "]");
                    state.set(Thread.State.TERMINATED);
                }
            }
        }, timeGap.get(), TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        stopFlag.set(true);
    }
}
