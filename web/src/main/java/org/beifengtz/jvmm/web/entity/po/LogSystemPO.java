package org.beifengtz.jvmm.web.entity.po;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import org.beifengtz.jvmm.core.entity.mx.SystemDynamicInfo;

/**
 * Description: TODO
 *
 * Created in 17:28 2022/2/25
 *
 * @author beifengtz
 */
@Data
@TableName("log_system_t")
public class LogSystemPO {
    private long id;
    private int nodeId;
    private long cvms;
    private long fpms;
    private long tpms;
    private long fsss;
    private long tsss;
    private double pcl;
    private long pct;
    private double scl;
    private double la;
    private long createTime;

    public void merge(SystemDynamicInfo info) {
        this.cvms = info.getCommittedVirtualMemorySize();
        this.fpms = info.getFreePhysicalMemorySize();
        this.tpms = info.getTotalPhysicalMemorySize();
        this.fsss = info.getFreeSwapSpaceSize();
        this.tsss = info.getTotalSwapSpaceSize();
        this.pcl = info.getProcessCpuLoad();
        this.scl = info.getSystemCpuLoad();
        this.la = info.getLoadAverage();
    }
}
