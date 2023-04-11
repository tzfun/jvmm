package org.beifengtz.jvmm.core.entity.info;

import java.lang.management.MemoryUsage;

/**
 * description: {@link MemoryUsage}对象的包装
 * <pre>
 *        +----------------------------------------------+
 *        +////////////////           |                  +
 *        +////////////////           |                  +
 *        +----------------------------------------------+
 *
 *        |--------|
 *           init
 *        |---------------|
 *               used
 *        |---------------------------|
 *                  committed
 *        |----------------------------------------------|
 *                            max
 * </pre>
 * date: 14:17 2023/4/11
 *
 * @author beifengtz
 */
public class MemoryUsageInfo {
    private long init;
    private long used;
    private long committed;
    private long max;

    public static MemoryUsageInfo parseFrom(MemoryUsage mu) {
        MemoryUsageInfo mui = new MemoryUsageInfo();
        if (mu == null) {
            return mui;
        }
        return mui.setInit(mu.getInit())
                .setCommitted(mu.getCommitted())
                .setUsed(mu.getUsed())
                .setMax(mu.getMax());
    }

    public long getInit() {
        return init;
    }

    public MemoryUsageInfo setInit(long init) {
        this.init = init;
        return this;
    }

    public long getUsed() {
        return used;
    }

    public MemoryUsageInfo setUsed(long used) {
        this.used = used;
        return this;
    }

    public long getCommitted() {
        return committed;
    }

    public MemoryUsageInfo setCommitted(long committed) {
        this.committed = committed;
        return this;
    }

    public long getMax() {
        return max;
    }

    public MemoryUsageInfo setMax(long max) {
        this.max = max;
        return this;
    }
}
