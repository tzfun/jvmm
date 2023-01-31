package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.core.entity.info.*;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
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

    SysInfo getSys();

    SysMemInfo getSysMem();

    void getCPU(Consumer<CPUInfo> consumer);

    void getNetwork(Consumer<NetInfo> consumer);

    List<DiskInfo> getDisk();

    List<OSFileInfo> getOSFile();

    ProcessInfo getProcess();

    JvmClassLoadingInfo getClassLoading();

    List<JvmClassLoaderInfo> getClassLoaders();

    JvmCompilationInfo getCompilation();

    List<JvmGCInfo> getGarbageCollector();

    List<JvmMemoryManagerInfo> getMemoryManager();

    List<JvmMemoryPoolInfo> getMemoryPool();

    JvmMemoryInfo getMemory();

    JvmThreadInfo getThreadDynamic();

    String getThreadInfo(long id);

    String[] getThreadInfo(long... ids);

    String getThreadInfo(long id, int maxDepth);

    String[] getThreadInfo(long[] ids, int maxDepth);

    String[] dumpAllThreads();
}
