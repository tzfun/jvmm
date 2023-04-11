package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * description: JVM 内存信息
 * date 14:48 2021/5/11
 *
 * @author beifengtz
 */
public class JvmMemoryInfo implements JsonParsable {
    /**
     * 堆内存使用情况
     */
    private MemoryUsageInfo heapUsage;
    /**
     * 非堆内存使用情况
     */
    private MemoryUsageInfo nonHeapUsage;
    /**
     * 挂起对象数（近似）
     */
    private int pendingCount;
    /**
     * 是否开启打印输出
     */
    private boolean verbose;

    private JvmMemoryInfo() {
    }

    public static JvmMemoryInfo create() {
        return new JvmMemoryInfo();
    }

    public MemoryUsageInfo getHeapUsage() {
        return heapUsage;
    }

    public void setHeapUsage(MemoryUsageInfo heapUsage) {
        this.heapUsage = heapUsage;
    }

    public MemoryUsageInfo getNonHeapUsage() {
        return nonHeapUsage;
    }

    public void setNonHeapUsage(MemoryUsageInfo nonHeapUsage) {
        this.nonHeapUsage = nonHeapUsage;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
