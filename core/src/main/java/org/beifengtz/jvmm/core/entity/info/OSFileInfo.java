package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * @author beifengtz
 * @description: 文件系统信息，包含各个磁盘分区的使用情况，磁盘类型
 * @date 14:21 2023/1/31
 */
public class OSFileInfo implements JsonParsable {

    private String name;
    private String mount;
    private String label;
    /**
     * 磁盘类型
     */
    private String type;
    /**
     * 总大小，bytes
     */
    private long size;
    /**
     * 总空闲大小，可能包含当前用户无法使用的空间（比如没有分配给此用户的空间），可用使用大小请查看{@link #usable}，bytes
     */
    private long free;
    /**
     * 当前用户可使用的大小，bytes
     */
    private long usable;

    private OSFileInfo() {
    }

    public static OSFileInfo create() {
        return new OSFileInfo();
    }

    public String getName() {
        return name;
    }

    public OSFileInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getMount() {
        return mount;
    }

    public OSFileInfo setMount(String mount) {
        this.mount = mount;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public OSFileInfo setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getType() {
        return type;
    }

    public OSFileInfo setType(String type) {
        this.type = type;
        return this;
    }

    public long getSize() {
        return size;
    }

    public OSFileInfo setSize(long size) {
        this.size = size;
        return this;
    }

    public long getFree() {
        return free;
    }

    public OSFileInfo setFree(long free) {
        this.free = free;
        return this;
    }

    public long getUsable() {
        return usable;
    }

    public OSFileInfo setUsable(long usable) {
        this.usable = usable;
        return this;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
