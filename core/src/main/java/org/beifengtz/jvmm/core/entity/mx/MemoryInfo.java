package org.beifengtz.jvmm.core.entity.mx;

import org.beifengtz.jvmm.core.entity.JsonParsable;

import java.lang.management.MemoryUsage;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 14:48 2021/5/11
 *
 * @author beifengtz
 */
public class MemoryInfo implements JsonParsable {
    /**
     * 堆内存使用情况
     */
    private MemoryUsage heapUsage;
    /**
     * 非堆内存使用情况
     */
    private MemoryUsage nonHeapUsage;
    /**
     * 挂起对象数（近似）
     */
    private int pendingCount;
    /**
     * 是否开启打印输出
     */
    private boolean verbose;

    private MemoryInfo() {
    }

    public static MemoryInfo create(){
        return new MemoryInfo();
    }

    public MemoryUsage getHeapUsage() {
        return heapUsage;
    }

    public void setHeapUsage(MemoryUsage heapUsage) {
        this.heapUsage = heapUsage;
    }

    public MemoryUsage getNonHeapUsage() {
        return nonHeapUsage;
    }

    public void setNonHeapUsage(MemoryUsage nonHeapUsage) {
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
