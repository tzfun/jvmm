package org.beifengtz.jvmm.web.entity.po;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import org.beifengtz.jvmm.core.entity.mx.ThreadDynamicInfo;

/**
 * Description: TODO
 *
 * Created in 17:59 2022/2/25
 *
 * @author beifengtz
 */
@Data
@TableName("log_thread_t")
public class LogThreadPO {
    private long id;
    private int nodeId;
    private int peak;
    private int daemon;
    private int current;
    private long totalStarted;
    private long createTime;

    public void merge(ThreadDynamicInfo info) {
        this.peak = info.getPeakThreadCount();
        this.daemon = info.getDaemonThreadCount();
        this.current = info.getThreadCount();
        this.totalStarted = info.getTotalStartedThreadCount();
    }
}
