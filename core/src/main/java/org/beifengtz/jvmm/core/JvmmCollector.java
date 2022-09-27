package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.core.entity.mx.*;

import java.util.List;

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

    List<ClassLoaderInfo> getClassLoaders();

    CompilationInfo getCompilation();

    List<GarbageCollectorInfo> getGarbageCollector();

    List<MemoryManagerInfo> getMemoryManager();

    List<MemoryPoolInfo> getMemoryPool();

    MemoryInfo getMemory();

    SystemDynamicInfo getSystemDynamic();

    ThreadDynamicInfo getThreadDynamic();

    String getThreadInfo(long id);

    String[] getThreadInfo(long... ids);

    String getThreadInfo(long id, int maxDepth);

    String[] getThreadInfo(long[] ids, int maxDepth);

    String[] dumpAllThreads();
}
