package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.core.entity.ClassLoadingInfo;
import org.beifengtz.jvmm.core.entity.CompilationInfo;
import org.beifengtz.jvmm.core.entity.GarbageCollectorInfo;
import org.beifengtz.jvmm.core.entity.MemoryInfo;
import org.beifengtz.jvmm.core.entity.MemoryManagerInfo;
import org.beifengtz.jvmm.core.entity.MemoryPoolInfo;
import org.beifengtz.jvmm.core.entity.ProcessInfo;
import org.beifengtz.jvmm.core.entity.SystemDynamicInfo;
import org.beifengtz.jvmm.core.entity.SystemStaticInfo;
import org.beifengtz.jvmm.core.entity.ThreadDynamicInfo;
import org.beifengtz.jvmm.core.service.DefaultScheduledService;
import org.beifengtz.jvmm.core.service.ScheduleService;

import java.util.function.Consumer;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:41 2021/5/11
 *
 * @author beifengtz
 */
class DefaultJvmmCollector implements JvmmCollector {

    private ScheduleService memoryTimer;
    private ScheduleService systemDynamicTimer;
    private ScheduleService threadDynamicTimer;

    DefaultJvmmCollector() {
    }

    @Override
    public SystemStaticInfo getSystemStatic() {
        return null;
    }

    @Override
    public ProcessInfo getProcess() {
        return null;
    }

    @Override
    public ClassLoadingInfo getClassLoading() {
        return null;
    }

    @Override
    public CompilationInfo getCompilation() {
        return null;
    }

    @Override
    public GarbageCollectorInfo getGarbageCollector() {
        return null;
    }

    @Override
    public MemoryManagerInfo getMemoryManager() {
        return null;
    }

    @Override
    public MemoryPoolInfo getMemoryPool() {
        return null;
    }

    @Override
    public MemoryInfo getMemory() {
        return null;
    }

    @Override
    public void timerGetMemory(int gapSeconds, Consumer<MemoryInfo> callback) {
        timerGetMemory(gapSeconds, -1, callback);
    }

    @Override
    public void timerGetMemory(int gapSeconds, int times, Consumer<MemoryInfo> callback) {
        if (memoryTimer == null) {
            memoryTimer = new DefaultScheduledService("Collect memory");
        }
        memoryTimer.setTask(() -> callback.accept(getMemory()))
                .setTimes(times)
                .setTimeGap(gapSeconds)
                .setStopOnError(true)
                .start();
    }

    @Override
    public void updateTimerGetMemory(int newGapSeconds) {
        if (memoryTimer != null) {
            memoryTimer.setTimeGap(newGapSeconds);
        }
    }

    @Override
    public SystemDynamicInfo getSystemDynamic() {
        return null;
    }

    @Override
    public void timerGetSystemDynamic(int gapSeconds, Consumer<SystemDynamicInfo> callback) {
        timerGetSystemDynamic(gapSeconds, -1, callback);
    }

    @Override
    public void timerGetSystemDynamic(int gapSeconds, int times, Consumer<SystemDynamicInfo> callback) {
        if (systemDynamicTimer == null) {
            systemDynamicTimer = new DefaultScheduledService("Collect system dynamic");
        }
        systemDynamicTimer.setTask(() -> callback.accept(getSystemDynamic()))
                .setTimes(times)
                .setTimeGap(gapSeconds)
                .setStopOnError(true)
                .start();
    }

    @Override
    public void updateTimerGetSystemDynamic(int newGapSeconds) {
        if (systemDynamicTimer != null) {
            systemDynamicTimer.setTimeGap(newGapSeconds);
        }
    }

    @Override
    public ThreadDynamicInfo getThreadDynamic() {
        return null;
    }

    @Override
    public void timerGetThreadDynamic(int gapSeconds, Consumer<ThreadDynamicInfo> callback) {
        timerGetThreadDynamic(gapSeconds, -1, callback);
    }

    @Override
    public void timerGetThreadDynamic(int gapSeconds, int times, Consumer<ThreadDynamicInfo> callback) {
        if (threadDynamicTimer == null) {
            threadDynamicTimer = new DefaultScheduledService("Collect thread dynamic");
        }
        threadDynamicTimer.setTask(() -> callback.accept(getThreadDynamic()))
                .setTimes(times)
                .setTimeGap(gapSeconds)
                .setStopOnError(true)
                .start();
    }

    @Override
    public void updateTimerGetThreadDynamic(int newGapSeconds) {
        if (threadDynamicTimer != null) {
            threadDynamicTimer.setTimeGap(newGapSeconds);
        }
    }

    @Override
    public String getThreadInfo(long id) {
        return null;
    }

    @Override
    public String[] getThreadInfo(long... ids) {
        return new String[0];
    }

    @Override
    public String getThreadInfo(long id, int maxDepth) {
        return null;
    }

    @Override
    public String[] getThreadInfo(long[] ids, int maxDepth) {
        return new String[0];
    }

    @Override
    public String[] dumpAllThreads() {
        return new String[0];
    }
}
