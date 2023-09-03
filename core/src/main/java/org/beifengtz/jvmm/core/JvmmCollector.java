package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.core.entity.info.*;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
     * @return {@link CPUInfo} of {@link CompletableFuture}
     */
    CompletableFuture<CPUInfo> getCPU();

    /**
     * 获取网卡、网络IO信息，异步执行
     *
     * @return {@link NetInfo} of {@link CompletableFuture}
     */
    CompletableFuture<NetInfo> getNetwork();

    /**
     * 获取所有磁盘信息
     *
     * @return {@link DiskInfo}列表
     */
    List<DiskInfo> getDisk();

    /**
     * 获取所有磁盘读写次数、吞吐量，异步执行
     *
     * @return {@link DiskIOInfo} List of {@link CompletableFuture}
     */
    CompletableFuture<List<DiskIOInfo>> getDiskIO();

    /**
     * 获取指定磁盘读写次数、吞吐量，异步执行
     *
     * @param name 磁盘名
     * @return {@link DiskIOInfo} of {@link CompletableFuture}
     */
    CompletableFuture<DiskIOInfo> getDiskIO(String name);

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
     * @return {@link JvmThreadDetailInfo}
     */
    JvmThreadDetailInfo getJvmThreadDetailInfo(long id);

    JvmThreadDetailInfo[] getJvmThreadDetailInfo(long... ids);

    JvmThreadDetailInfo[] getAllJvmThreadDetailInfo();

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
     *
     * @return {@link String}[] 线程堆栈
     */
    String[] getJvmDeadlockThreadStack();

    /**
     * dump JVM当前运行的所有线程堆栈信息
     *
     * @return {@link String}[] 线程堆栈
     */
    String[] dumpAllThreads();

    /**
     * 获取线程池信息
     *
     * @param threadPool {@link ThreadPoolExecutor}
     * @return {@link ThreadPoolInfo}，如果 threadPool 为 null 将返回 null
     */
    ThreadPoolInfo getThreadPoolInfo(ThreadPoolExecutor threadPool);

    /**
     * 根据提供的类加载器、类路径、变量信息反射获取 {@link ThreadPoolExecutor} 对象并采集其信息，仅支持静态属性！
     *
     * @param classLoader 类加载器
     * @param clazz       类全路径，比如 org.beifengtz.jvmm.common.factory.ExecutorFactory
     * @param filed       属性名，仅支持静态属性！比如 SCHEDULE_THREAD_POOL
     * @return {@link ThreadPoolInfo}，如果反射未找到相应的 threadPool 或其值为 null 将返回 null
     */
    ThreadPoolInfo getThreadPoolInfo(ClassLoader classLoader, String clazz, String filed);

    /**
     * 根据提供的类加载器、类路径、变量信息反射获取 {@link ThreadPoolExecutor} 对象并采集其信息
     *
     * @param classLoader   类加载器
     * @param clazz         类全路径，比如 org.beifengtz.jvmm.common.factory.ExecutorFactory
     * @param instanceField 指定类实例对象的属性名，比如单例模式中的 INSTANCE
     * @param filed         实例对象中的属性名
     * @return {@link ThreadPoolInfo}，如果反射未找到相应的 threadPool 或其值为 null 将返回 null
     */
    ThreadPoolInfo getThreadPoolInfo(ClassLoader classLoader, String clazz, String instanceField, String filed);

    /**
     * 根据提供的类、变量信息反射获取 {@link ThreadPoolExecutor} 对象并采集其信息
     *
     * @param clazz 类全路径，比如 org.beifengtz.jvmm.common.factory.ExecutorFactory
     * @param filed 属性名，仅支持静态属性！比如 SCHEDULE_THREAD_POOL
     * @return {@link ThreadPoolInfo}，如果反射未找到相应的 threadPool 或其值为 null 将返回 null
     */
    ThreadPoolInfo getThreadPoolInfo(String clazz, String filed);

    /**
     * 根据提供的类路径、变量信息反射获取 {@link ThreadPoolExecutor} 对象并采集其信息
     *
     * @param clazz         类全路径，比如 org.beifengtz.jvmm.common.factory.ExecutorFactory
     * @param instanceField 指定类实例对象的属性名，比如单例模式中的 INSTANCE
     * @param filed         实例对象中的属性名
     * @return {@link ThreadPoolInfo}，如果反射未找到相应的 threadPool 或其值为 null 将返回 null
     */
    ThreadPoolInfo getThreadPoolInfo(String clazz, String instanceField, String filed);

    /**
     * 获取指定端口列表的端口运行状态
     *
     * @param ports 端口数组
     * @return {@link PortInfo}
     */
    PortInfo getPortInfo(int... ports);

    /**
     * 获取指定端口列表的端口运行状态
     *
     * @param portList 端口列表
     * @return {@link PortInfo}
     */
    PortInfo getPortInfo(Collection<Integer> portList);

    /**
     * 从调用时开始统计，经过指定时间间隔之后，计算每一个线程的 CPU Time 和 User Time，返回结果根据 CPU Time从大到小排序。
     * 通常 CPU Time 越大表明此线程占用 CPU 时间越多，该线程越繁忙。
     *
     * @param time 间隔时间
     * @param unit 间隔时间单位
     * @return 根据 CPU Time 从大到小排序的 {@link ThreadTimedInfo} 列表，它将以 {@link CompletableFuture} 返回
     */
    CompletableFuture<List<ThreadTimedInfo>> getOrderedThreadTimedInfo(long time, TimeUnit unit);

    /**
     * 从调用时开始统计，经过指定时间间隔之后，计算每一个线程的 CPU Time 和 User Time，返回结果根据 CPU Time从大到小排序。
     * 通常 CPU Time 越大表明此线程占用 CPU 时间越多，该线程越繁忙。
     * 返回所有线程的堆栈信息
     *
     * @param time 间隔时间
     * @param unit 间隔时间单位
     * @return 根据 CPU Time 从大到小排序的线程堆栈列表，它将以 {@link CompletableFuture} 返回
     */
    CompletableFuture<List<String>> getOrderedThreadTimedStack(long time, TimeUnit unit);
}
