package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.core.entity.info.*;

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

    SysInfo getSys();

    SysMemInfo getSysMem();

    void getCPU(Consumer<CPUInfo> consumer);

    void getNetwork(Consumer<NetInfo> consumer);

    List<DiskInfo> getDisk();

    List<SysFileInfo> getSysFile();

    ProcessInfo getProcess();

    JvmClassLoadingInfo getJvmClassLoading();

    List<JvmClassLoaderInfo> getJvmClassLoaders();

    JvmCompilationInfo getJvmCompilation();

    List<JvmGCInfo> getJvmGC();

    List<JvmMemoryManagerInfo> getJvmMemoryManager();

    List<JvmMemoryPoolInfo> getJvmMemoryPool();

    JvmMemoryInfo getJvmMemory();

    JvmThreadInfo getJvmThread();

    String getJvmThreadStack(long id);

    String[] getJvmThreadStack(long... ids);

    String getJvmThreadStack(long id, int maxDepth);

    String[] getJvmThreadStack(long[] ids, int maxDepth);

    String[] dumpAllThreads();
}
