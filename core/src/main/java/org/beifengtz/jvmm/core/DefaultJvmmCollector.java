package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.common.exception.ExecutionException;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.util.ExecuteNativeUtil;
import org.beifengtz.jvmm.common.util.PidUtil;
import org.beifengtz.jvmm.common.util.PlatformUtil;
import org.beifengtz.jvmm.core.entity.mx.*;
import org.beifengtz.jvmm.core.entity.result.LinuxMem;
import org.slf4j.Logger;

import java.io.File;
import java.lang.management.*;
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

    private static final Logger log = LoggerFactory.logger(DefaultJvmmCollector.class);

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
    public List<ClassLoaderInfo> getClassLoaders() {
        try {
            return Unsafe.getClassLoaders();
        } catch (Exception e) {
            throw new ExecutionException("Can not load classloaders: " + e.getMessage(), e);
        }
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

        try {
            com.sun.management.OperatingSystemMXBean sunSystemMXBean = (com.sun.management.OperatingSystemMXBean) operatingSystemMXBean;
            info.setCommittedVirtualMemorySize(sunSystemMXBean.getCommittedVirtualMemorySize());
            info.setFreePhysicalMemorySize(sunSystemMXBean.getFreePhysicalMemorySize());
            info.setFreeSwapSpaceSize(sunSystemMXBean.getFreeSwapSpaceSize());
            info.setProcessCpuLoad(sunSystemMXBean.getProcessCpuLoad());
            info.setProcessCpuTime(sunSystemMXBean.getProcessCpuTime());
            info.setSystemCpuLoad(sunSystemMXBean.getSystemCpuLoad());
            info.setTotalPhysicalMemorySize(sunSystemMXBean.getTotalPhysicalMemorySize());
            info.setTotalSwapSpaceSize(sunSystemMXBean.getTotalSwapSpaceSize());

            if (PlatformUtil.isLinux()) {
                LinuxMem linuxMemoryResult = getLinuxMemoryResult();
                info.setBufferCacheMemorySize(linuxMemoryResult.getBuffCache());
                info.setSharedMemorySize(linuxMemoryResult.getShared());
            }

            File[] files = File.listRoots();
            for (File file : files) {
                info.addDisk(SystemDynamicInfo.DiskInfo.create()
                        .setName(file.getCanonicalPath())
                        .setTotal(file.getTotalSpace())
                        .setUsable(file.getUsableSpace()));
            }

        } catch (Throwable e) {
            log.warn("Get system dynamic info failed. " + e.getMessage(), e);
        }

        info.setLoadAverage(operatingSystemMXBean.getSystemLoadAverage());

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

    private LinuxMem getLinuxMemoryResult() {
        List<String> results = ExecuteNativeUtil.execute("free -b");
        LinuxMem result = new LinuxMem();
        if (results.size() > 1) {
            String[] split = results.get(1).split("\\s+");
            if (split.length > 6) {
                result.setTotal(Long.parseLong(split[1]));
                result.setUsed(Long.parseLong(split[2]));
                result.setFree(Long.parseLong(split[3]));
                result.setShared(Long.parseLong(split[4]));
                result.setBuffCache(Long.parseLong(split[5]));
                result.setAvailable(Long.parseLong(split[6]));
            }
        }
        return result;
    }
}
