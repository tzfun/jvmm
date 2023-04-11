package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * author beifengtz
 * description: 系统内存信息
 * @date 15:17 2023/1/31
 */
public class SysMemInfo implements JsonParsable {
    /**
     * 已提交的虚拟内存大小，bytes
     */
    private long committedVirtual;
    /**
     * 总物理内存大小，bytes
     */
    private long totalPhysical;
    /**
     * 可用物理内存大小，bytes
     */
    private long freePhysical;
    /**
     * 总swap空间大小，bytes
     */
    private long totalSwap;
    /**
     * 可用swap空间大小，bytes
     */
    private long freeSwap;
    /**
     * 缓冲区内存大小，仅限linux系统下，bytes
     */
    private long bufferCache;
    /**
     * 共享内存大小，bytes
     */
    private long shared;

    private SysMemInfo() {

    }

    public static SysMemInfo create() {
        return new SysMemInfo();
    }

    public long getCommittedVirtual() {
        return committedVirtual;
    }

    public SysMemInfo setCommittedVirtual(long committedVirtual) {
        this.committedVirtual = committedVirtual;
        return this;
    }

    public long getTotalPhysical() {
        return totalPhysical;
    }

    public SysMemInfo setTotalPhysical(long totalPhysical) {
        this.totalPhysical = totalPhysical;
        return this;
    }

    public long getFreePhysical() {
        return freePhysical;
    }

    public SysMemInfo setFreePhysical(long freePhysical) {
        this.freePhysical = freePhysical;
        return this;
    }

    public long getTotalSwap() {
        return totalSwap;
    }

    public SysMemInfo setTotalSwap(long totalSwap) {
        this.totalSwap = totalSwap;
        return this;
    }

    public long getFreeSwap() {
        return freeSwap;
    }

    public SysMemInfo setFreeSwap(long freeSwap) {
        this.freeSwap = freeSwap;
        return this;
    }

    public long getBufferCache() {
        return bufferCache;
    }

    public SysMemInfo setBufferCache(long bufferCache) {
        this.bufferCache = bufferCache;
        return this;
    }

    public long getShared() {
        return shared;
    }

    public SysMemInfo setShared(long shared) {
        this.shared = shared;
        return this;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
