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
    private int frequency;
    private boolean pickCl;
    private boolean pickC;
    private boolean pickP;
    private boolean pickGc;
    private boolean pickMm;
    private boolean pickMp;
    private boolean pickM;
    private boolean pickSd;
    private boolean pickTd;

    public void check() {
        if (frequency < 0) {
            frequency = 0;
        }
    }
}
