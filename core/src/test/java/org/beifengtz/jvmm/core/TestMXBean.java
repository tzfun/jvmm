package org.beifengtz.jvmm.core;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Description: TODO View MX Bean
 * </p>
 * <p>
 * Created in 9:46 2021/5/11
 *
 * @author beifengtz
 */
public class TestMXBean {

    @Test
    public void testAll() {
        testMemory();
        testRuntime();
        testMemoryManager();
        testMemoryPool();
        testClassLoading();
        testCompilation();
        testOperatingSystem();
        testOperatingSystemExtra();
        testThread();
        testThreadInfo();
        testGarbageCollector();
    }

    @Test
    public void testMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        System.err.println(memoryMXBean.getHeapMemoryUsage());
        System.err.println(memoryMXBean.getNonHeapMemoryUsage());
        //  返回其终止被挂起的对象的近似数目
        System.err.println(memoryMXBean.getObjectPendingFinalizationCount());
        //  测试内存系统的 verbose 输出是否已启用
        System.err.println(memoryMXBean.isVerbose());
    }

    @Test
    public void testRuntime() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        System.err.println(runtimeMXBean.getVmVersion());
        System.err.println(runtimeMXBean.getVmVendor());
        System.err.println(runtimeMXBean.getVmName());
        System.err.println(runtimeMXBean.getBootClassPath());
        System.err.println(runtimeMXBean.getClassPath());
        System.err.println(runtimeMXBean.getInputArguments());
        System.err.println(runtimeMXBean.getLibraryPath());
        System.err.println(runtimeMXBean.getManagementSpecVersion());
        System.err.println(runtimeMXBean.getName());
        System.err.println(runtimeMXBean.getSpecName());
        System.err.println(runtimeMXBean.getSpecVendor());
        System.err.println(runtimeMXBean.getSpecVersion());
        System.err.println(runtimeMXBean.getStartTime());
        System.err.println(runtimeMXBean.getUptime());
        System.err.println(runtimeMXBean.getSystemProperties());
    }

    @Test
    public void testMemoryManager() {
        //  内存管理器
        List<MemoryManagerMXBean> memoryManagerMXBeans = ManagementFactory.getMemoryManagerMXBeans();
        memoryManagerMXBeans.forEach(b -> {
            System.err.println(b.getName());
            System.err.println(Arrays.toString(b.getMemoryPoolNames()));
            System.err.println(b.isValid());
            System.err.println("---><----");
        });
    }

    @Test
    public void testMemoryPool() {
        //  各个内存池的详情
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        memoryPoolMXBeans.forEach(b -> {
            System.err.println("-------------><-----------------");
            System.err.println(b.getName());
            System.err.println(b.getCollectionUsage());
            System.err.println(Arrays.toString(b.getMemoryManagerNames()));
            System.err.println(b.isValid());
            if (b.isCollectionUsageThresholdSupported()) {
                System.err.println(b.isCollectionUsageThresholdExceeded());
                System.err.println(b.getCollectionUsageThreshold());
                System.err.println(b.getCollectionUsageThresholdCount());
            }

            System.err.println(b.getPeakUsage());
            System.err.println(b.getType());
            System.err.println(b.getUsage());
            if (b.isUsageThresholdSupported()) {
                System.err.println(b.isUsageThresholdExceeded());
                System.err.println(b.getUsageThreshold());
                System.err.println(b.getUsageThresholdCount());
            }
        });
    }

    @Test
    public void testClassLoading() {
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        System.err.println(classLoadingMXBean.getLoadedClassCount());
        System.err.println(classLoadingMXBean.isVerbose());
        System.err.println(classLoadingMXBean.getTotalLoadedClassCount());
        System.err.println(classLoadingMXBean.getUnloadedClassCount());
    }

    @Test
    public void testCompilation() {
        CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
        System.err.println(compilationMXBean.getName());
        if (compilationMXBean.isCompilationTimeMonitoringSupported()) {
            System.err.println(compilationMXBean.getTotalCompilationTime());
        }
    }

    @Test
    public void testOperatingSystem() {
        //  提供 JVM 所运行的操作系统信息
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        System.err.println(operatingSystemMXBean.getName());
        System.err.println(operatingSystemMXBean.getArch());
        System.err.println(operatingSystemMXBean.getSystemLoadAverage());
        System.err.println(operatingSystemMXBean.getAvailableProcessors());
        System.err.println(operatingSystemMXBean.getVersion());
    }

    @Test
    public void testOperatingSystemExtra() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers())) {
                Object value;
                try {
                    value = method.invoke(operatingSystemMXBean);
                } catch (Exception e) {
                    value = e;
                }
                System.err.println(method.getName() + " = " + value);
            }
        }
    }

    @Test
    public void testThread() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        System.err.println(Arrays.toString(threadMXBean.findDeadlockedThreads()));
        System.err.println(Arrays.toString(threadMXBean.getAllThreadIds()));
        System.err.println(threadMXBean.getThreadCount());
        System.err.println(Arrays.toString(threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds())));
        if (threadMXBean.isCurrentThreadCpuTimeSupported()) {
            System.err.println(threadMXBean.getCurrentThreadCpuTime());
        }

        System.err.println(threadMXBean.getCurrentThreadUserTime());
        System.err.println(threadMXBean.getDaemonThreadCount());
        System.err.println(threadMXBean.getPeakThreadCount());
        System.err.println(threadMXBean.getTotalStartedThreadCount());
        System.err.println(Arrays.toString(threadMXBean.dumpAllThreads(false, false)));
    }

    @Test
    public void testThreadInfo() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();


        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);

        System.out.println(Arrays.toString(threadInfos));
        System.err.println(new Gson().toJson(threadInfos));
    }

    @Test
    public void testGarbageCollector() {
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        garbageCollectorMXBeans.forEach(b -> {
            System.err.println("---------><-------------");
            System.err.println(b.getName());
            System.err.println(b.getCollectionCount());
            System.err.println(b.getCollectionTime());
            System.err.println(b.isValid());
            System.err.println(Arrays.toString(b.getMemoryPoolNames()));
        });
    }
}
