package org.beifengtz.jvmm.web.entity.po;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryInfo;
import org.beifengtz.jvmm.core.entity.info.MemoryUsageInfo;

/**
 * Description: TODO
 * <p>
 * Created in 17:54 2022/2/25
 *
 * @author beifengtz
 */
@Data
@TableName("log_memory_t")
public class LogMemoryPO {
    private long id;
    private int nodeId;
    private boolean verbose;
    private long pendingCount;
    private long heapInit;
    private long heapUsed;
    private long heapCommitted;
    private long heapMax;
    private long nonHeapInit;
    private long nonHeapUsed;
    private long nonHeapCommitted;
    private long nonHeapMax;
    private long createTime;

    public void merge(JvmMemoryInfo info) {
        this.verbose = info.isVerbose();
        this.pendingCount = info.getPendingCount();
        MemoryUsageInfo heapUsage = info.getHeapUsage();
        if (heapUsage != null) {
            this.heapInit = heapUsage.getInit();
            this.heapUsed = heapUsage.getUsed();
            this.heapCommitted = heapUsage.getCommitted();
            this.heapMax = heapUsage.getMax();
        }

        MemoryUsageInfo nonHeapUsage = info.getNonHeapUsage();
        if (nonHeapUsage != null) {
            this.nonHeapInit = nonHeapUsage.getInit();
            this.nonHeapUsed = nonHeapUsage.getUsed();
            this.nonHeapCommitted = nonHeapUsage.getCommitted();
            this.nonHeapMax = nonHeapUsage.getMax();
        }
    }

}
