package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * @author beifengtz
 * @description: 文件系统信息，包含各个磁盘分区的使用情况，磁盘类型
 * @date 14:21 2023/1/31
 */
public class SysFileInfo implements JsonParsable {

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

    private SysFileInfo() {
    }

    public static SysFileInfo create() {
        return new SysFileInfo();
    }

    public String getName() {
        return name;
    }

    public SysFileInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getMount() {
        return mount;
    }

    public SysFileInfo setMount(String mount) {
        this.mount = mount;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public SysFileInfo setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getType() {
        return type;
    }

    public SysFileInfo setType(String type) {
        this.type = type;
        return this;
    }

    public long getSize() {
        return size;
    }

    public SysFileInfo setSize(long size) {
        this.size = size;
        return this;
    }

    public long getFree() {
        return free;
    }

    public SysFileInfo setFree(long free) {
        this.free = free;
        return this;
    }

    public long getUsable() {
        return usable;
    }

    public SysFileInfo setUsable(long usable) {
        this.usable = usable;
        return this;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
