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
import org.beifengtz.jvmm.tools.util.PidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

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

    private static final Logger log = LoggerFactory.getLogger(DefaultJvmmCollector.class);

    DefaultJvmmCollector() {
    }

    @Override
    public SystemStaticInfo getSystemStatic() {
        SystemStaticInfo info = SystemStaticInfo.create();
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        info.setName(operatingSystemMXBean.getName());
        info.setArch(operatingSystemMXBean.getArch());
        info.setVersion(operatingSystemMXBean.getVersion());
        info.setAvailableProcessors(operatingSystemMXBean.getAvailableProcessors());
        info.setLoadAverage(operatingSystemMXBean.getSystemLoadAverage());
        info.setTimeZone(TimeZone.getDefault().toZoneId().toString());
        return info;
    }

    @Override
    public ProcessInfo getProcess() {
        ProcessInfo info = ProcessInfo.create();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        info.setName(runtimeMXBean.getName());
        info.setStartTime(runtimeMXBean.getStartTime());
        info.setUptime(runtimeMXBean.getUptime());
        info.setPid(PidUtil.currentPid());
        info.setVmName(runtimeMXBean.getVmName());
        info.setVmVendor(runtimeMXBean.getVmVendor());
        info.setVmVersion(runtimeMXBean.getVmVersion());
        boolean bootClassPathSupported = runtimeMXBean.isBootClassPathSupported();
        if (bootClassPathSupported) {
            info.setBootClassPath(runtimeMXBean.getBootClassPath());
        }
        info.setBootClassPathSupported(bootClassPathSupported);
        info.setClassPath(runtimeMXBean.getClassPath());
        info.setLibraryPath(runtimeMXBean.getLibraryPath());
        info.setInputArgs(runtimeMXBean.getInputArguments());
        info.setSystemProperties(runtimeMXBean.getSystemProperties());

        info.setManagementSpecVersion(runtimeMXBean.getManagementSpecVersion());
        info.setSpecName(runtimeMXBean.getSpecName());
        info.setSpecVendor(runtimeMXBean.getSpecVendor());
        info.setSpecVersion(runtimeMXBean.getSpecVersion());
        return info;
    }

    @Override
    public ClassLoadingInfo getClassLoading() {
        ClassLoadingInfo info = ClassLoadingInfo.create();
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        info.setVerbose(classLoadingMXBean.isVerbose());
        info.setLoadedClassCount(classLoadingMXBean.getLoadedClassCount());
        info.setTotalLoadedClassCount(classLoadingMXBean.getTotalLoadedClassCount());
        info.setUnLoadedClassCount(classLoadingMXBean.getUnloadedClassCount());
        return info;
    }

    @Override
    public CompilationInfo getCompilation() {
        CompilationInfo info = CompilationInfo.create();
        CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
        info.setName(compilationMXBean.getName());
        boolean compilationTimeMonitoringSupported = compilationMXBean.isCompilationTimeMonitoringSupported();
        if (compilationTimeMonitoringSupported) {
            info.setTotalCompilationTime(compilationMXBean.getTotalCompilationTime());
        }
        info.setTimeMonitoringSupported(compilationTimeMonitoringSupported);
        return info;
    }

    @Override
    public List<GarbageCollectorInfo> getGarbageCollector() {
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        List<GarbageCollectorInfo> infos = new ArrayList<>(garbageCollectorMXBeans.size());
        for (GarbageCollectorMXBean b : garbageCollectorMXBeans) {
            GarbageCollectorInfo info = GarbageCollectorInfo.create();
            info.setName(b.getName());
            info.setValid(b.isValid());
            info.setCollectionCount(b.getCollectionCount());
            info.setCollectionTime(b.getCollectionTime());
            info.setMemoryPoolNames(b.getMemoryPoolNames());
            infos.add(info);
        }
        return infos;
    }

    @Override
    public List<MemoryManagerInfo> getMemoryManager() {
        List<MemoryManagerMXBean> memoryManagerMXBeans = ManagementFactory.getMemoryManagerMXBeans();
        List<MemoryManagerInfo> infos = new ArrayList<>(memoryManagerMXBeans.size());
        for (MemoryManagerMXBean b : memoryManagerMXBeans) {
            MemoryManagerInfo info = MemoryManagerInfo.create();
            info.setName(b.getName());
            info.setValid(b.isValid());
            info.setMemoryPoolNames(b.getMemoryPoolNames());
            infos.add(info);
        }
        return infos;
    }

    @Override
    public List<MemoryPoolInfo> getMemoryPool() {
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        List<MemoryPoolInfo> infos = new ArrayList<>(memoryPoolMXBeans.size());
        for (MemoryPoolMXBean b : memoryPoolMXBeans) {
            MemoryPoolInfo info = MemoryPoolInfo.create();
            info.setName(b.getName());
            info.setValid(b.isValid());
            info.setManagerNames(b.getMemoryManagerNames());
            info.setType(b.getType());
            info.setUsage(b.getUsage());
            info.setPeakUsage(b.getPeakUsage());
            info.setCollectionUsage(b.getCollectionUsage());

            boolean collectionUsageThresholdSupported = b.isCollectionUsageThresholdSupported();
            if (collectionUsageThresholdSupported) {
                info.setCollectionUsageThreshold(b.getCollectionUsageThreshold());
                info.setCollectionUsageThresholdCount(b.getCollectionUsageThresholdCount());
                info.setCollectionUsageThresholdExceeded(b.isCollectionUsageThresholdExceeded());
            }
            info.setCollectionUsageThresholdSupported(collectionUsageThresholdSupported);

            boolean usageThresholdSupported = b.isUsageThresholdSupported();
            if (usageThresholdSupported) {
                info.setUsageThreshold(b.getUsageThreshold());
                info.setUsageThresholdCount(b.getUsageThresholdCount());
                info.setUsageThresholdExceeded(b.isUsageThresholdExceeded());
            }
            info.setUsageThresholdSupported(usageThresholdSupported);

            infos.add(info);
        }
        return infos;
    }

    @Override
    public MemoryInfo getMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryInfo info = MemoryInfo.create();
        info.setVerbose(memoryMXBean.isVerbose());
        info.setHeapUsage(memoryMXBean.getHeapMemoryUsage());
        info.setPendingCount(memoryMXBean.getObjectPendingFinalizationCount());
        info.setNonHeapUsage(memoryMXBean.getNonHeapMemoryUsage());
        return info;
    }

    @Override
    public SystemDynamicInfo getSystemDynamic() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        SystemDynamicInfo info = SystemDynamicInfo.create();
        Class<? extends OperatingSystemMXBean> clazz = operatingSystemMXBean.getClass();

        try {
            Method committedVirtualMemorySize = clazz.getMethod("getCommittedVirtualMemorySize");
            info.setCommittedVirtualMemorySize((long) committedVirtualMemorySize.invoke(operatingSystemMXBean));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Get system dynamic info failed. [getCommittedVirtualMemorySize] " + e.getMessage(), e);
        }

        try {
            Method freePhysicalMemorySize = clazz.getMethod("getFreePhysicalMemorySize");
            info.setFreePhysicalMemorySize((long) freePhysicalMemorySize.invoke(operatingSystemMXBean));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Get system dynamic info failed. [getFreePhysicalMemorySize] " + e.getMessage(), e);
        }

        try {
            Method freeSwapSpaceSize = clazz.getMethod("getFreeSwapSpaceSize");
            info.setFreeSwapSpaceSize((long) freeSwapSpaceSize.invoke(operatingSystemMXBean));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Get system dynamic info failed. [getFreeSwapSpaceSize] " + e.getMessage(), e);
        }

        try {
            Method processCpuLoad = clazz.getMethod("getProcessCpuLoad");
            info.setProcessCpuLoad((double) processCpuLoad.invoke(operatingSystemMXBean));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Get system dynamic info failed. [getProcessCpuLoad] " + e.getMessage(), e);
        }

        try {
            Method processCpuTime = clazz.getMethod("getProcessCpuTime");
            info.setProcessCpuTime((long) processCpuTime.invoke(operatingSystemMXBean));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Get system dynamic info failed. [getProcessCpuTime] " + e.getMessage(), e);
        }

        try {
            Method systemCpuLoad = clazz.getMethod("getSystemCpuLoad");
            info.setSystemCpuLoad((double) systemCpuLoad.invoke(operatingSystemMXBean));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Get system dynamic info failed. [getSystemCpuLoad] " + e.getMessage(), e);
        }

        try {
            Method totalPhysicalMemorySize = clazz.getMethod("getTotalPhysicalMemorySize");
            info.setTotalSwapSpaceSize((long) totalPhysicalMemorySize.invoke(operatingSystemMXBean));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Get system dynamic info failed. [getTotalPhysicalMemorySize] " + e.getMessage(), e);
        }

        try {
            Method totalSwapSpaceSize = clazz.getMethod("getTotalSwapSpaceSize");
            info.setTotalSwapSpaceSize((long) totalSwapSpaceSize.invoke(operatingSystemMXBean));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Get system dynamic info failed. [getTotalSwapSpaceSize] " + e.getMessage(), e);
        }
        return info;
    }

    @Override
    public ThreadDynamicInfo getThreadDynamic() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadDynamicInfo info = ThreadDynamicInfo.create();
        info.setThreadCount(threadMXBean.getThreadCount());
        info.setTotalStartedThreadCount(threadMXBean.getTotalStartedThreadCount());
        info.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());
        info.setDeadlockedThreads(threadMXBean.findDeadlockedThreads());
        info.setPeakThreadCount(threadMXBean.getPeakThreadCount());
        return info;
    }

    @Override
    public String getThreadInfo(long id) {
        return getThreadInfo(id, 0);
    }

    @Override
    public String[] getThreadInfo(long... ids) {
        return getThreadInfo(ids, 0);
    }

    @Override
    public String getThreadInfo(long id, int maxDepth) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo info = threadMXBean.getThreadInfo(id, maxDepth);
        if (Objects.nonNull(info)) {
            return info.toString();
        }
        return null;
    }

    @Override
    public String[] getThreadInfo(long[] ids, int maxDepth) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfo = threadMXBean.getThreadInfo(ids, maxDepth);
        return threadInfo2Str(threadInfo);
    }

    @Override
    public String[] dumpAllThreads() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfo = threadMXBean.dumpAllThreads(false, false);
        return threadInfo2Str(threadInfo);
    }

    private String[] threadInfo2Str(ThreadInfo[] threadInfo) {
        if (threadInfo == null) {
            return new String[0];
        }
        String[] result = new String[threadInfo.length];
        for (int i = 0; i < threadInfo.length; i++) {
            ThreadInfo info = threadInfo[i];
            if (Objects.nonNull(info)) {
                result[i] = info.toString();
            } else {
                result[i] = null;
            }
        }
        return result;
    }
}
