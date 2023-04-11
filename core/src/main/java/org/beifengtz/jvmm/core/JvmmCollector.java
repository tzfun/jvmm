package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.core.entity.info.*;

import java.util.List;
import java.util.function.Consumer;

/**
 * <p>
 * Description: 收集器接口
 * </p>
 * Created in 16:32 2021/5/11
 *
 * @author beifengtz
 */
public interface JvmmCollector {

    /**
     * 获取硬件和操作系统信息
     *
     * @return {@link SysInfo}
     */
    SysInfo getSys();

    /**
     * 获取操作系统内存信息
     *
     * @return {@link SysMemInfo}
     */
    SysMemInfo getSysMem();

    /**
     * 获取CPU负载信息，异步执行
     *
     * @param consumer 计算成功时回调{@link CPUInfo}
     */
    void getCPU(Consumer<CPUInfo> consumer);

    /**
     * 获取网卡、网络IO信息，异步执行
     *
     * @param consumer 计算成功时回调{@link NetInfo}
     */
    void getNetwork(Consumer<NetInfo> consumer);

    /**
     * 获取所有磁盘信息
     *
     * @return  {@link DiskInfo}列表
     */
    List<DiskInfo> getDisk();

    /**
     * 获取所有磁盘读写次数、吞吐量，异步执行
     *
     * @param consumer 计算成功时回调所有的磁盘信息{@link DiskIOInfo}
     */
    void getDiskIO(Consumer<List<DiskIOInfo>> consumer);

    /**
     * 获取指定磁盘读写次数、吞吐量，异步执行
     *
     * @param name     磁盘名
     * @param consumer 计算成功时回调该磁盘的{@link DiskIOInfo}
     */
    void getDiskIO(String name, Consumer<DiskIOInfo> consumer);

    /**
     * 获取磁盘分区信息
     *
     * @return {@link SysFileInfo}列表
     */
    List<SysFileInfo> getSysFile();

    /**
     * 获取当前运行的进程信息
     *
     * @return {@link ProcessInfo}
     */
    ProcessInfo getProcess();

    /**
     * 获取JVM Class Loading信息
     *
     * @return {@link JvmClassLoadingInfo}
     */
    JvmClassLoadingInfo getJvmClassLoading();

    /**
     * 获取JVM Class Loader信息
     *
     * @return {@link JvmClassLoaderInfo}
     */
    List<JvmClassLoaderInfo> getJvmClassLoaders();

    /**
     * 获取JVM JNI编译信息
     *
     * @return {@link JvmCompilationInfo}
     */
    JvmCompilationInfo getJvmCompilation();

    /**
     * 获取JVM GC详情
     *
     * @return {@link JvmGCInfo}
     */
    List<JvmGCInfo> getJvmGC();

    /**
     * 获取JVM 各个内存管理器信息
     *
     * @return {@link JvmMemoryManagerInfo}列表
     */
    List<JvmMemoryManagerInfo> getJvmMemoryManager();

    /**
     * 获取JVM各个内存池信息
     *
     * @return {@link JvmMemoryPoolInfo}列表
     */
    List<JvmMemoryPoolInfo> getJvmMemoryPool();

    /**
     * 获取 JVM 当前内存信息
     *
     * @return {@link JvmMemoryInfo}
     */
    JvmMemoryInfo getJvmMemory();

    /**
     * 获取 JVM 线程运行数
     *
     * @return {@link JvmThreadInfo}
     */
    JvmThreadInfo getJvmThread();

    /**
     * 获取 JVM 线程统计信息，部分信息需要开启线程监控：
     * {@link JvmmExecutor#setThreadCpuTimeEnabled(boolean)}
     * {@link JvmmExecutor#setThreadContentionMonitoringEnabled(boolean)}
     *
     * @param id 线程id
     * @return {@link JvmThreadStatisticInfo}
     */
    JvmThreadStatisticInfo getJvmThreadStatisticInfo(long id);

    JvmThreadStatisticInfo[] getJvmThreadStatisticInfo(long... ids);

    JvmThreadStatisticInfo[] getAllJvmThreadStatisticInfo();

    /**
     * 获取指定线程ID的堆栈
     *
     * @param id 线程ID
     * @return {@link String} 线程堆栈
     */
    String getJvmThreadStack(long id);

    /**
     * 获取多个线程ID的堆栈
     *
     * @param ids 线程ID数组
     * @return {@link String}[] 线程堆栈
     */
    String[] getJvmThreadStack(long... ids);

    /**
     * 获取指定线程ID的堆栈，可以自定义堆栈深度
     *
     * @param id       线程ID
     * @param maxDepth 堆栈深度
     * @return {@link String} 线程堆栈
     */
    String getJvmThreadStack(long id, int maxDepth);

    /**
     * 获取多个线程ID的堆栈，可以自定义堆栈深度
     *
     * @param ids      线程ID数组
     * @param maxDepth 堆栈深度
     * @return {@link String}[] 线程堆栈
     */
    String[] getJvmThreadStack(long[] ids, int maxDepth);

    /**
     * 获取死锁线程堆栈
     * @return {@link String}[] 线程堆栈
     */
    String[] getJvmDeadlockThreadStack();

    /**
     * dump JVM当前运行的所有线程堆栈信息
     *
     * @return {@link String}[] 线程堆栈
     */
    String[] dumpAllThreads();
}
