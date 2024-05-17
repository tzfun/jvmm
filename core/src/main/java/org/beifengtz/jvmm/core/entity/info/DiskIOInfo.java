package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * description: 磁盘IO信息
 * date 14:42 2023/2/1
 * @author beifengtz
 */
public class DiskIOInfo implements JsonParsable {

    /**
     * 磁盘名
     */
    private String name;
    /**
     * 读次数
     */
    private double reads;
    /**
     * 写次数
     */
    private double writes;
    /**
     * 读大小 bytes/s
     */
    private double readBytes;
    /**
     * 写大小 bytes/s
     */
    private double writeBytes;
    /**
     * 磁盘队列长度
     */
    private long currentQueueLength;

    private DiskIOInfo() {

    }

    public static DiskIOInfo create() {
        return new DiskIOInfo();
    }

    public String getName() {
        return name;
    }

    public DiskIOInfo setName(String name) {
        this.name = name;
        return this;
    }

    public double getReads() {
        return reads;
    }

    public DiskIOInfo setReads(double reads) {
        this.reads = reads;
        return this;
    }

    public double getWrites() {
        return writes;
    }

    public DiskIOInfo setWrites(double writes) {
        this.writes = writes;
        return this;
    }

    public double getReadBytes() {
        return readBytes;
    }

    public DiskIOInfo setReadBytes(double readBytes) {
        this.readBytes = readBytes;
        return this;
    }

    public double getWriteBytes() {
        return writeBytes;
    }

    public DiskIOInfo setWriteBytes(double writeBytes) {
        this.writeBytes = writeBytes;
        return this;
    }

    public long getCurrentQueueLength() {
        return currentQueueLength;
    }

    public DiskIOInfo setCurrentQueueLength(long currentQueueLength) {
        this.currentQueueLength = currentQueueLength;
        return this;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
