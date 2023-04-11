package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

import java.util.ArrayList;
import java.util.List;

/**
 * description: 磁盘信息类
 * date 11:23 2023/1/31
 * @author beifengtz
 */
public class DiskInfo implements JsonParsable {
    /**
     * 磁盘名
     */
    private String name;
    /**
     * 磁盘模型
     */
    private String model;
    /**
     * 总大小，bytes
     */
    private long size;
    /**
     * 当前磁盘队列长度，包括已向设备发出的IO请求
     */
    private long currentQueueLength;

    /**
     * 分区信息
     */
    private final List<DiskPartition> partitions = new ArrayList<>();

    protected DiskInfo() {
    }

    public static DiskInfo create() {
        return new DiskInfo();
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public long getSize() {
        return size;
    }

    public long getCurrentQueueLength() {
        return currentQueueLength;
    }

    public List<DiskPartition> getPartitions() {
        return partitions;
    }

    public DiskInfo setName(String name) {
        this.name = name;
        return this;
    }

    public DiskInfo setModel(String model) {
        this.model = model;
        return this;
    }

    public DiskInfo setSize(long size) {
        this.size = size;
        return this;
    }

    public DiskInfo setCurrentQueueLength(long currentQueueLength) {
        this.currentQueueLength = currentQueueLength;
        return this;
    }

    public DiskInfo addPartition(DiskPartition partition) {
        this.partitions.add(partition);
        return this;
    }

    public DiskInfo clearPartitions() {
        this.partitions.clear();
        return this;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }

    public static class DiskPartition implements JsonParsable {
        /**
         * 挂载位置
         */
        private String mount;
        /**
         * 分区唯一ID
         */
        private String identification;
        /**
         * 分区大小，bytes
         */
        private long size;

        private DiskPartition() {
        }

        public static DiskPartition create() {
            return new DiskPartition();
        }

        public String getMount() {
            return mount;
        }

        public DiskPartition setMount(String mount) {
            this.mount = mount;
            return this;
        }

        public String getIdentification() {
            return identification;
        }

        public DiskPartition setIdentification(String identification) {
            this.identification = identification;
            return this;
        }

        public long getSize() {
            return size;
        }

        public DiskPartition setSize(long size) {
            this.size = size;
            return this;
        }

        @Override
        public String toString() {
            return toJsonStr();
        }
    }
}
