package org.beifengtz.jvmm.core.entity;

import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 15:40 2021/5/11
 *
 * @author beifengtz
 */
public class MemoryPoolInfo {
    private String name;
    private String isValid;
    private String[] managerNames;
    /**
     * 内存类型
     */
    private MemoryType type;
    /**
     * 内存池的内存使用量估计值
     */
    private MemoryUsage usage;
    /**
     * Java虚拟机最近为回收该内存池中未使用的对象而花费的精力之后的内存使用情况
     */
    private MemoryUsage collectionUsage;
    /**
     * 自启动Java虚拟机或重置峰值以来此内存池的峰值内存使用情况
     */
    private MemoryUsage peakUsage;

    private boolean usageThresholdSupported;
    private boolean usageThresholdExceeded;
    /**
     * 此内存池的使用阈值
     */
    private long usageThreshold;
    /**
     * 内存使用超过使用阈值的次数
     */
    private long usageThresholdCount;

    private boolean collectionUsageThresholdSupported;
    private boolean collectionUsageThresholdExceeded;
    /**
     * 此内存池的收集使用阈值（byte）
     */
    private long collectionUsageThreshold;
    /**
     * Java虚拟机检测到内存使用量已达到或超过收集使用量阈值的次数
     */
    private long collectionUsageThresholdCount;
}
