package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.core.entity.mx.ClassLoadingInfo;
import org.beifengtz.jvmm.core.entity.mx.CompilationInfo;
import org.beifengtz.jvmm.core.entity.mx.GarbageCollectorInfo;
import org.beifengtz.jvmm.core.entity.mx.MemoryInfo;
import org.beifengtz.jvmm.core.entity.mx.MemoryManagerInfo;
import org.beifengtz.jvmm.core.entity.mx.MemoryPoolInfo;
import org.beifengtz.jvmm.core.entity.mx.ProcessInfo;
import org.beifengtz.jvmm.core.entity.mx.SystemDynamicInfo;
import org.beifengtz.jvmm.core.entity.mx.SystemStaticInfo;
import org.beifengtz.jvmm.core.entity.mx.ThreadDynamicInfo;

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
