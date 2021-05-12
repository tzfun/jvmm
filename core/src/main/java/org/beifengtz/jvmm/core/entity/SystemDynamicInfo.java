package org.beifengtz.jvmm.core.entity;

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
    private long totalPhysicalMemorySize;
    private long totalSwapSpaceSize;

    private SystemDynamicInfo(){
    }

    public static SystemDynamicInfo create(){
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

    @Override
    public String toString() {
        return toJsonStr();
    }
}
