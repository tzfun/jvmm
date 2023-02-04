package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * @author beifengtz
 * @description: 磁盘IO信息
 * @date 14:42 2023/2/1
 */
public class DiskIOInfo implements JsonParsable {

    /**
     * 磁盘名
     */
    private String name;
    /**
     * 每秒读次数
     */
    private double readPerSecond;
    /**
     * 每秒写次数
     */
    private double writePerSecond;
    /**
     * 读速率 bytes/s
     */
    private double readBytesPerSecond;
    /**
     * 写速率 bytes/s
     */
    private double writeBytesPerSecond;
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

    public double getReadPerSecond() {
        return readPerSecond;
    }

    public long getCurrentQueueLength() {
        return currentQueueLength;
    }

    public DiskIOInfo setCurrentQueueLength(long currentQueueLength) {
        this.currentQueueLength = currentQueueLength;
        return this;
    }

    public DiskIOInfo setReadPerSecond(double readPerSecond) {
        this.readPerSecond = readPerSecond;
        return this;
    }

    public double getWritePerSecond() {
        return writePerSecond;
    }

    public DiskIOInfo setWritePerSecond(double writePerSecond) {
        this.writePerSecond = writePerSecond;
        return this;
    }

    public double getReadBytesPerSecond() {
        return readBytesPerSecond;
    }

    public DiskIOInfo setReadBytesPerSecond(double readBytesPerSecond) {
        this.readBytesPerSecond = readBytesPerSecond;
        return this;
    }

    public double getWriteBytesPerSecond() {
        return writeBytesPerSecond;
    }

    public DiskIOInfo setWriteBytesPerSecond(double writeBytesPerSecond) {
        this.writeBytesPerSecond = writeBytesPerSecond;
        return this;
    }


    @Override
    public String toString() {
        return toJsonStr();
    }
}
