package org.beifengtz.jvmm.web.entity.po;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import org.beifengtz.jvmm.core.entity.info.JvmClassLoadingInfo;

/**
 * Description: TODO
 *
 * Created in 17:35 2022/2/25
 *
 * @author beifengtz
 */
@Data
@TableName("log_classloading_t")
public class LogClassloadingPO {
    private long id;
    private int nodeId;
    private boolean verbose;
    private int lcc;
    private long ulcc;
    private long tlcc;
    private long createTime;

    public void merge(JvmClassLoadingInfo info) {
        this.verbose = info.isVerbose();
        this.lcc = info.getLoadedClassCount();
        this.ulcc = info.getUnLoadedClassCount();
        this.tlcc = info.getTotalLoadedClassCount();
    }

}
