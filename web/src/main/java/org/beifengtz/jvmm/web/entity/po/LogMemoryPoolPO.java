package org.beifengtz.jvmm.web.entity.po;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryPoolInfo;
import org.beifengtz.jvmm.core.entity.info.MemoryUsageInfo;

/**
 * Description: TODO
 * <p>
 * Created in 17:49 2022/2/25
 *
 * @author beifengtz
 */
@Data
@TableName("log_memory_pool_t")
public class LogMemoryPoolPO {
    private long id;
    private int nodeId;
    private String name;
    private boolean valid;
    private String managerNames;
    private String type;
    private long init;
    private long used;
    private long committed;
    private long max;
    private long createTime;

    public void merge(JvmMemoryPoolInfo info) {
        this.name = info.getName();
        this.valid = info.getValid();
        this.managerNames = StringUtil.join(",", info.getManagerNames());
        this.type = info.getType().toString();
        MemoryUsageInfo usage = info.getUsage();
        if (usage != null) {
            this.init = usage.getInit();
            this.used = usage.getUsed();
            this.committed = usage.getCommitted();
            this.max = usage.getMax();
        }
    }

}
