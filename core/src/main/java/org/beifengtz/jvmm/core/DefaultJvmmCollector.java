package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.common.exception.ExecutionException;
import org.beifengtz.jvmm.common.util.IPUtil;
import org.beifengtz.jvmm.common.util.PidUtil;
import org.beifengtz.jvmm.common.util.PlatformUtil;
import org.beifengtz.jvmm.common.util.SystemPropertyUtil;
import org.beifengtz.jvmm.core.driver.OSDriver;
import org.beifengtz.jvmm.core.entity.info.*;
import org.beifengtz.jvmm.core.entity.result.LinuxMemResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.State;
import java.lang.management.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
    public SysInfo getSys() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        return SysInfo.create().setName(operatingSystemMXBean.getName())
                .setArch(operatingSystemMXBean.getArch())
                .setVersion(operatingSystemMXBean.getVersion())
                .setCpuNum(operatingSystemMXBean.getAvailableProcessors())
                .setTimeZone(SystemPropertyUtil.get("user.timezone"))
                .setIp(IPUtil.getLocalIP())
                .setUser(SystemPropertyUtil.get("user.name"));
    }

    @Override
    public SysMemInfo getSysMem() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        SysMemInfo info = SysMemInfo.create();
        try {
            com.sun.management.OperatingSystemMXBean sunSystemMXBean = (com.sun.management.OperatingSystemMXBean) operatingSystemMXBean;
            info.setCommittedVirtual(sunSystemMXBean.getCommittedVirtualMemorySize())
                    .setFreePhysical(sunSystemMXBean.getFreePhysicalMemorySize())
                    .setFreeSwap(sunSystemMXBean.getFreePhysicalMemorySize())
                    .setTotalPhysical(sunSystemMXBean.getTotalPhysicalMemorySize())
                    .setTotalSwap(sunSystemMXBean.getTotalSwapSpaceSize());

            if (PlatformUtil.isLinux()) {
                LinuxMemResult linuxMemoryResult = OSDriver.get().getLinuxMemoryInfo();
                info.setBufferCache(linuxMemoryResult.getBuffCache());
                info.setShared(linuxMemoryResult.getShared());
            }
        } catch (Throwable e) {
            log.warn("Get system dynamic info failed. " + e.getMessage(), e);
        }
        return info;
    }

    @Override
    public void getCPU(Consumer<CPUInfo> consumer) {
        OSDriver.get().getCPUInfo(consumer);
    }

    @Override
    public void getNetwork(Consumer<NetInfo> consumer) {
        OSDriver.get().getNetInfo(consumer);
    }

    @Override
    public List<DiskInfo> getDisk() {
        return OSDriver.get().getDiskInfo();
    }

    @Override
    public void getDiskIO(Consumer<List<DiskIOInfo>> consumer) {
        OSDriver.get().getDiskIOInfo(consumer);
    }

    @Override
    public void getDiskIO(String name, Consumer<DiskIOInfo> consumer) {
        OSDriver.get().getDiskIOInfo(name, consumer);
    }

    @Override
    public List<SysFileInfo> getSysFile() {
        return OSDriver.get().getOsFileInfo();
    }

    @Override
    public ProcessInfo getProcess() {
        ProcessInfo info = ProcessInfo.create();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        info.setName(runtimeMXBean.getName())
                .setStartTime(runtimeMXBean.getStartTime())
                .setUptime(runtimeMXBean.getUptime())
                .setPid(PidUtil.currentPid())
                .setVmName(runtimeMXBean.getVmName())
                .setVmVendor(runtimeMXBean.getVmVendor())
                .setVmVersion(runtimeMXBean.getVmVersion())
                .setVmHome(SystemPropertyUtil.get("java.home"))
                .setVmManagementSpecVersion(runtimeMXBean.getManagementSpecVersion())
                .setVmSpecName(runtimeMXBean.getSpecName())
                .setVmSpecVendor(runtimeMXBean.getSpecVendor())
                .setVmSpecVersion(runtimeMXBean.getSpecVersion())
                .setWorkDir(SystemPropertyUtil.get("user.dir"))
                .setInputArgs(runtimeMXBean.getInputArguments());
        return info;
    }

    @Override
    public JvmClassLoadingInfo getJvmClassLoading() {
        JvmClassLoadingInfo info = JvmClassLoadingInfo.create();
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        info.setVerbose(classLoadingMXBean.isVerbose());
        info.setLoadedClassCount(classLoadingMXBean.getLoadedClassCount());
        info.setTotalLoadedClassCount(classLoadingMXBean.getTotalLoadedClassCount());
        info.setUnLoadedClassCount(classLoadingMXBean.getUnloadedClassCount());
        return info;
    }

    @Override
    public List<JvmClassLoaderInfo> getJvmClassLoaders() {
        try {
            return Unsafe.getClassLoaders();
        } catch (Exception e) {
            throw new ExecutionException("Can not load classloaders: " + e.getMessage(), e);
        }
    }

    @Override
    public JvmCompilationInfo getJvmCompilation() {
        JvmCompilationInfo info = JvmCompilationInfo.create();
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
    public List<JvmGCInfo> getJvmGC() {
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        List<JvmGCInfo> infos = new ArrayList<>(garbageCollectorMXBeans.size());
        for (GarbageCollectorMXBean b : garbageCollectorMXBeans) {
            JvmGCInfo info = JvmGCInfo.create();
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
    public List<JvmMemoryManagerInfo> getJvmMemoryManager() {
        List<MemoryManagerMXBean> memoryManagerMXBeans = ManagementFactory.getMemoryManagerMXBeans();
        List<JvmMemoryManagerInfo> infos = new ArrayList<>(memoryManagerMXBeans.size());
        for (MemoryManagerMXBean b : memoryManagerMXBeans) {
            JvmMemoryManagerInfo info = JvmMemoryManagerInfo.create();
            info.setName(b.getName());
            info.setValid(b.isValid());
            info.setMemoryPoolNames(b.getMemoryPoolNames());
            infos.add(info);
        }
        return infos;
    }

    @Override
    public List<JvmMemoryPoolInfo> getJvmMemoryPool() {
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        List<JvmMemoryPoolInfo> infos = new ArrayList<>(memoryPoolMXBeans.size());
        for (MemoryPoolMXBean b : memoryPoolMXBeans) {
            JvmMemoryPoolInfo info = JvmMemoryPoolInfo.create();
            info.setName(b.getName());
            info.setValid(b.isValid());
            info.setManagerNames(b.getMemoryManagerNames());
            info.setType(b.getType());
            info.setUsage(MemoryUsageInfo.parseFrom(b.getUsage()));
            info.setPeakUsage(MemoryUsageInfo.parseFrom(b.getPeakUsage()));
            info.setCollectionUsage(MemoryUsageInfo.parseFrom(b.getCollectionUsage()));

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
    public JvmMemoryInfo getJvmMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        JvmMemoryInfo info = JvmMemoryInfo.create();
        info.setVerbose(memoryMXBean.isVerbose());
        info.setHeapUsage(MemoryUsageInfo.parseFrom(memoryMXBean.getHeapMemoryUsage()));
        info.setPendingCount(memoryMXBean.getObjectPendingFinalizationCount());
        info.setNonHeapUsage(MemoryUsageInfo.parseFrom(memoryMXBean.getNonHeapMemoryUsage()));
        return info;
    }

    @Override
    public JvmThreadInfo getJvmThread() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        JvmThreadInfo info = JvmThreadInfo.create();
        info.setThreadCount(threadMXBean.getThreadCount());
        info.setTotalStartedThreadCount(threadMXBean.getTotalStartedThreadCount());
        info.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());
        info.setDeadlockedThreads(threadMXBean.findDeadlockedThreads());
        info.setPeakThreadCount(threadMXBean.getPeakThreadCount());

        try {
            for (Thread thread : Unsafe.getThreads()) {
                if ("DestroyJavaVM".equals(thread.getName())) {
                    continue;
                }
                ThreadGroup group = thread.getThreadGroup();
                if (group == null || !"system".equals(group.getName())) {
                    continue;
                }
                info.increaseStateCount(thread.getState());
            }
        } catch (Exception e) {
            log.error("Collect thread info failed: " + e.getMessage(), e);
        }
        return info;
    }

    @Override
    public JvmThreadStatisticInfo getJvmThreadStatisticInfo(long id) {
        return getJvmThreadStatisticInfo(ManagementFactory.getThreadMXBean(), id);
    }

    @Override
    public JvmThreadStatisticInfo[] getJvmThreadStatisticInfo(long... ids) {
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        JvmThreadStatisticInfo[] res = new JvmThreadStatisticInfo[ids.length];
        for (int i = 0; i < ids.length; i++) {
            res[i] = getJvmThreadStatisticInfo(mx, ids[i]);
        }
        return res;
    }

    @Override
    public JvmThreadStatisticInfo[] getAllJvmThreadStatisticInfo() {
        return getJvmThreadStatisticInfo(ManagementFactory.getThreadMXBean().getAllThreadIds());
    }

    @Override
    public String getJvmThreadStack(long id) {
        return getJvmThreadStack(id, 0);
    }

    @Override
    public String[] getJvmThreadStack(long... ids) {
        return getJvmThreadStack(ids, 0);
    }

    @Override
    public String getJvmThreadStack(long id, int maxDepth) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo info = threadMXBean.getThreadInfo(id, maxDepth);
        return threadInfo2Str(threadMXBean, info);
    }

    @Override
    public String[] getJvmThreadStack(long[] ids, int maxDepth) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfo = threadMXBean.getThreadInfo(ids, maxDepth);
        String[] res = new String[threadInfo.length];
        for (int i = 0; i < threadInfo.length; i++) {
            res[i] = threadInfo2Str(threadMXBean, threadInfo[i]);
        }
        return res;
    }

    @Override
    public String[] getJvmDeadlockThreadStack() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        String[] res = new String[deadlockedThreads.length];
        for (int i = 0; i < deadlockedThreads.length; i++) {
            res[i] = threadInfo2Str(threadMXBean, threadMXBean.getThreadInfo(deadlockedThreads[i], 10));
        }
        return res;
    }

    @Override
    public String[] dumpAllThreads() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfo = threadMXBean.dumpAllThreads(true, true);
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        String[] res = new String[threadInfo.length + deadlockedThreads.length + 1];
        int i = 0;
        for (; i < threadInfo.length; i++) {
            res[i] = threadInfo2Str(threadMXBean, threadInfo[i]);
        }

        res[i++] = "\r\nDeadlock found between the following threads: \r\n";

        for (long deadlockedThread : deadlockedThreads) {
            res[i++] = threadInfo2Str(threadMXBean, threadMXBean.getThreadInfo(deadlockedThread, 10));
        }

        return res;
    }

    private static String threadInfo2Str(ThreadMXBean threadMXBean, ThreadInfo ti) {
        if (ti == null) return null;
        StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\"");
        sb.append(" Id=").append(ti.getThreadId());
        sb.append(" cpu=").append(threadMXBean.getThreadCpuTime(ti.getThreadId())).append(" ns");
        sb.append(" usr=").append(threadMXBean.getThreadUserTime(ti.getThreadId())).append(" ns");
        sb.append(" blocked ").append(ti.getBlockedCount()).append(" for ").append(ti.getBlockedTime()).append(" ms");
        sb.append(" waited ").append(ti.getWaitedCount()).append(" for ").append(ti.getWaitedTime()).append(" ms");

        if (ti.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (ti.isInNative()) {
            sb.append(" (running in native)");
        }
        sb.append("\r\n");
        sb.append("   state: ").append(ti.getThreadState());
        sb.append("\r\n");

        for (LockInfo li : ti.getLockedSynchronizers()) {
            sb.append("\tlocks ").append(li.toString()).append("\r\n");
        }
        boolean start = true;
        StackTraceElement[] stes = ti.getStackTrace();
        Object[] monitorDepths = new Object[stes.length];
        MonitorInfo[] mis = ti.getLockedMonitors();
        for (MonitorInfo monitorInfo : mis) {
            monitorDepths[monitorInfo.getLockedStackDepth()] = monitorInfo;
        }
        for (int i = 0; i < stes.length; i++) {
            StackTraceElement ste = stes[i];
            sb.append("\tat ").append(ste.toString()).append("\r\n");
            if (start) {
                if (ti.getLockName() != null) {
                    sb.append("\t").append("- waiting on (a ").append(ti.getLockName()).append(")");
                    if (ti.getLockOwnerName() != null) {
                        sb.append(" owned by ").append(ti.getLockOwnerName()).append(" Id=").append(ti.getLockOwnerId());
                    }
                    sb.append("\r\n");
                }
                start = false;
            }
            if (monitorDepths[i] != null) {
                MonitorInfo mi = (MonitorInfo) monitorDepths[i];
                sb.append("\t- locked (a ")
                        .append(mi.toString())
                        .append(")")
                        .append(" index ")
                        .append(mi.getLockedStackDepth())
                        .append(" frame ")
                        .append(mi.getLockedStackFrame().toString())
                        .append("\r\n");
            }
        }
        return sb.toString();
    }

    private static JvmThreadStatisticInfo getJvmThreadStatisticInfo(ThreadMXBean threadMXBean, long id) {
        JvmThreadStatisticInfo info = JvmThreadStatisticInfo.create();
        ThreadInfo ti = threadMXBean.getThreadInfo(id);
        info.setId(id)
                .setName(ti.getThreadName())
                .setState(ti.getThreadState())
                .setUserTime(threadMXBean.getThreadUserTime(id))
                .setCpuTime(threadMXBean.getThreadCpuTime(id))
                .setBlockedCount(ti.getBlockedCount())
                .setBlockedTime(ti.getBlockedTime())
                .setWaitedCount(ti.getWaitedCount())
                .setWaitedTime(ti.getWaitedTime());

        LockInfo[] lockedSynchronizers = ti.getLockedSynchronizers();
        String[] locks = new String[lockedSynchronizers.length];
        for (int i = 0; i < lockedSynchronizers.length; i++) {
            locks[i] = lockedSynchronizers[i].toString();
        }
        info.setLocks(locks);

        return info;
    }

}
