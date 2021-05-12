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

import java.util.List;
import java.util.function.Consumer;

/**
 * <p>
 * Description: TODO Jvmm收集器接口
 * </p>
 * <p>
 * Created in 16:32 2021/5/11
 *
 * @author beifengtz
 */
public interface JvmmCollector {

    SystemStaticInfo getSystemStatic();

    ProcessInfo getProcess();

    ClassLoadingInfo getClassLoading();

    CompilationInfo getCompilation();

    List<GarbageCollectorInfo> getGarbageCollector();

    List<MemoryManagerInfo> getMemoryManager();

    List<MemoryPoolInfo> getMemoryPool();

    MemoryInfo getMemory();

    void timerGetMemory(int gapSeconds, Consumer<MemoryInfo> callback);

    void timerGetMemory(int gapSeconds, int times, Consumer<MemoryInfo> callback);

    void updateTimerGetMemory(int newGapSeconds);

    SystemDynamicInfo getSystemDynamic();

    void timerGetSystemDynamic(int gapSeconds, Consumer<SystemDynamicInfo> callback);

    void timerGetSystemDynamic(int gapSeconds, int times, Consumer<SystemDynamicInfo> callback);

    void updateTimerGetSystemDynamic(int newGapSeconds);

    ThreadDynamicInfo getThreadDynamic();

    void timerGetThreadDynamic(int gapSeconds, Consumer<ThreadDynamicInfo> callback);

    void timerGetThreadDynamic(int gapSeconds, int times, Consumer<ThreadDynamicInfo> callback);

    void updateTimerGetThreadDynamic(int newGapSeconds);

    String getThreadInfo(long id);

    String[] getThreadInfo(long... ids);

    String getThreadInfo(long id, int maxDepth);

    String[] getThreadInfo(long[] ids, int maxDepth);

    String[] dumpAllThreads();
}
