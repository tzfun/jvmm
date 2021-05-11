package org.beifengtz.jvmm.core.entity;

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
public class MemoryInfo {
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
}
