package org.beifengtz.jvmm.web.entity.po;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.core.entity.info.JvmGCInfo;

/**
 * Description: TODO
 *
 * Created in 17:38 2022/2/25
 *
 * @author beifengtz
 */
@Data
@TableName("log_gc_t")
public class LogGcPO {
    private long id;
    private int nodeId;
    private String name;
    private boolean valid;
    private long gcCount;
    private long gcTime;
    private String memoryPool;
    private long createTime;

    public void merge(JvmGCInfo info) {
        this.name = info.getName();
        this.valid = info.isValid();
        this.gcCount = info.getCollectionCount();
        this.gcTime = info.getCollectionTime();
        this.memoryPool = StringUtil.join(",", info.getMemoryPoolNames());
    }
}
