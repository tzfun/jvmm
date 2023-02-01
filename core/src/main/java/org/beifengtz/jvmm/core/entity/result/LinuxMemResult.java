package org.beifengtz.jvmm.core.entity.result;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:09 2022/9/1
 *
 * @author beifengtz
 */
public class LinuxMemResult {
    private long total;
    private long used;
    private long free;
    private long shared;
    private long buffCache;
    private long available;

    public long getTotal() {
        return total;
    }

    public LinuxMemResult setTotal(long total) {
        this.total = total;
        return this;
    }

    public long getUsed() {
        return used;
    }

    public LinuxMemResult setUsed(long used) {
        this.used = used;
        return this;
    }

    public long getFree() {
        return free;
    }

    public LinuxMemResult setFree(long free) {
        this.free = free;
        return this;
    }

    public long getShared() {
        return shared;
    }

    public LinuxMemResult setShared(long shared) {
        this.shared = shared;
        return this;
    }

    public long getBuffCache() {
        return buffCache;
    }

    public LinuxMemResult setBuffCache(long buffCache) {
        this.buffCache = buffCache;
        return this;
    }

    public long getAvailable() {
        return available;
    }

    public LinuxMemResult setAvailable(long available) {
        this.available = available;
        return this;
    }
}
