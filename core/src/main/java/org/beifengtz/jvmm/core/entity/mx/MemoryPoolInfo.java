package org.beifengtz.jvmm.core.entity.mx;

import org.beifengtz.jvmm.core.entity.JsonParsable;

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
public class MemoryPoolInfo implements JsonParsable {
    private String name;
    private boolean valid;
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

    private MemoryPoolInfo() {
    }

    public static MemoryPoolInfo create() {
        return new MemoryPoolInfo();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String[] getManagerNames() {
        return managerNames;
    }

    public void setManagerNames(String[] managerNames) {
        this.managerNames = managerNames;
    }

    public MemoryType getType() {
        return type;
    }

    public void setType(MemoryType type) {
        this.type = type;
    }

    public MemoryUsage getUsage() {
        return usage;
    }

    public void setUsage(MemoryUsage usage) {
        this.usage = usage;
    }

    public MemoryUsage getCollectionUsage() {
        return collectionUsage;
    }

    public void setCollectionUsage(MemoryUsage collectionUsage) {
        this.collectionUsage = collectionUsage;
    }

    public MemoryUsage getPeakUsage() {
        return peakUsage;
    }

    public void setPeakUsage(MemoryUsage peakUsage) {
        this.peakUsage = peakUsage;
    }

    public boolean isUsageThresholdSupported() {
        return usageThresholdSupported;
    }

    public void setUsageThresholdSupported(boolean usageThresholdSupported) {
        this.usageThresholdSupported = usageThresholdSupported;
    }

    public boolean isUsageThresholdExceeded() {
        return usageThresholdExceeded;
    }

    public void setUsageThresholdExceeded(boolean usageThresholdExceeded) {
        this.usageThresholdExceeded = usageThresholdExceeded;
    }

    public long getUsageThreshold() {
        return usageThreshold;
    }

    public void setUsageThreshold(long usageThreshold) {
        this.usageThreshold = usageThreshold;
    }

    public long getUsageThresholdCount() {
        return usageThresholdCount;
    }

    public void setUsageThresholdCount(long usageThresholdCount) {
        this.usageThresholdCount = usageThresholdCount;
    }

    public boolean isCollectionUsageThresholdSupported() {
        return collectionUsageThresholdSupported;
    }

    public void setCollectionUsageThresholdSupported(boolean collectionUsageThresholdSupported) {
        this.collectionUsageThresholdSupported = collectionUsageThresholdSupported;
    }

    public boolean isCollectionUsageThresholdExceeded() {
        return collectionUsageThresholdExceeded;
    }

    public void setCollectionUsageThresholdExceeded(boolean collectionUsageThresholdExceeded) {
        this.collectionUsageThresholdExceeded = collectionUsageThresholdExceeded;
    }

    public long getCollectionUsageThreshold() {
        return collectionUsageThreshold;
    }

    public void setCollectionUsageThreshold(long collectionUsageThreshold) {
        this.collectionUsageThreshold = collectionUsageThreshold;
    }

    public long getCollectionUsageThresholdCount() {
        return collectionUsageThresholdCount;
    }

    public void setCollectionUsageThresholdCount(long collectionUsageThresholdCount) {
        this.collectionUsageThresholdCount = collectionUsageThresholdCount;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
