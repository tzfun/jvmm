package org.beifengtz.jvmm.core.entity.mx;

import org.beifengtz.jvmm.common.JsonParsable;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 15:21 2021/5/11
 *
 * @author beifengtz
 */
public class SystemDynamicInfo implements JsonParsable {
    /**
     * 已提交的虚拟内存大小
     */
    private long committedVirtualMemorySize;
    /**
     * 可用物理内存大小
     */
    private long freePhysicalMemorySize;
    /**
     * 可用swap空间大小
     */
    private long freeSwapSpaceSize;
    /**
     * 进程cpu负载
     */
    private double processCpuLoad;
    /**
     * 进程cpu时间
     */
    private long processCpuTime;
    /**
     * 系统cpu负载
     */
    private double systemCpuLoad;
    /**
     * 最后一分钟的系统平均负载，如果为负值表示不可用
     */
    private double loadAverage;

    private long totalPhysicalMemorySize;
    private long totalSwapSpaceSize;
    /**
     * 缓冲区内存大小，仅限linux系统下
     */
    private long bufferCacheSize;
    private long sharedSize;
    private final List<DiskInfo> disks = new ArrayList<>();

    private SystemDynamicInfo() {
    }

    public static SystemDynamicInfo create() {
        return new SystemDynamicInfo();
    }

    public long getCommittedVirtualMemorySize() {
        return committedVirtualMemorySize;
    }

    public void setCommittedVirtualMemorySize(long committedVirtualMemorySize) {
        this.committedVirtualMemorySize = committedVirtualMemorySize;
    }

    public long getFreePhysicalMemorySize() {
        return freePhysicalMemorySize;
    }

    public void setFreePhysicalMemorySize(long freePhysicalMemorySize) {
        this.freePhysicalMemorySize = freePhysicalMemorySize;
    }

    public long getFreeSwapSpaceSize() {
        return freeSwapSpaceSize;
    }

    public void setFreeSwapSpaceSize(long freeSwapSpaceSize) {
        this.freeSwapSpaceSize = freeSwapSpaceSize;
    }

    public double getProcessCpuLoad() {
        return processCpuLoad;
    }

    public void setProcessCpuLoad(double processCpuLoad) {
        this.processCpuLoad = processCpuLoad;
    }

    public long getProcessCpuTime() {
        return processCpuTime;
    }

    public void setProcessCpuTime(long processCpuTime) {
        this.processCpuTime = processCpuTime;
    }

    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public void setSystemCpuLoad(double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    public long getTotalPhysicalMemorySize() {
        return totalPhysicalMemorySize;
    }

    public void setTotalPhysicalMemorySize(long totalPhysicalMemorySize) {
        this.totalPhysicalMemorySize = totalPhysicalMemorySize;
    }

    public long getTotalSwapSpaceSize() {
        return totalSwapSpaceSize;
    }

    public void setTotalSwapSpaceSize(long totalSwapSpaceSize) {
        this.totalSwapSpaceSize = totalSwapSpaceSize;
    }

    public double getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
    }

    public long getBufferCacheSize() {
        return bufferCacheSize;
    }

    public void setBufferCacheSize(long bufferCacheSize) {
        this.bufferCacheSize = bufferCacheSize;
    }

    public long getSharedSize() {
        return sharedSize;
    }

    public void setSharedSize(long sharedSize) {
        this.sharedSize = sharedSize;
    }

    public List<DiskInfo> getDisks() {
        return disks;
    }

    public void addDisk(DiskInfo disk) {
        disks.add(disk);
    }

    public void clearDisk() {
        disks.clear();
    }

    @Override
    public String toString() {
        return toJsonStr();
    }

    public static class DiskInfo implements JsonParsable {
        private String name;
        private long total;
        private long usable;

        private DiskInfo() {
        }

        public static DiskInfo create() {
            return new DiskInfo();
        }

        public String getName() {
            return name;
        }

        public DiskInfo setName(String name) {
            this.name = name;
            return this;
        }

        public long getTotal() {
            return total;
        }

        public DiskInfo setTotal(long total) {
            this.total = total;
            return this;
        }

        public long getUsable() {
            return usable;
        }

        public DiskInfo setUsable(long usable) {
            this.usable = usable;
            return this;
        }

        @Override
        public String toString() {
            return toJsonStr();
        }
    }
}
