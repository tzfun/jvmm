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
public class SystemDynamicInfo {
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
}
