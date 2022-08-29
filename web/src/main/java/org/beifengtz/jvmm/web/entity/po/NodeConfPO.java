package org.beifengtz.jvmm.web.entity.po;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

/**
 * Description: TODO
 *
 * Created in 11:16 2022/2/25
 *
 * @author beifengtz
 */
@Data
@TableName("node_conf_t")
public class NodeConfPO {
    private int id;
    private boolean auto;
    private boolean store;
    private int frequency;
    private boolean pickClassloading;
    private boolean pickGc;
    private boolean pickMemoryPool;
    private boolean pickMemory;
    private boolean pickSystem;
    private boolean pickThread;

    public void check() {
        if (frequency < 0) {
            frequency = 0;
        }
    }
}
