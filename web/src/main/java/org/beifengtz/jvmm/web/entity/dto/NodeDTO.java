package org.beifengtz.jvmm.web.entity.dto;

import lombok.Data;
import org.beifengtz.jvmm.common.util.AssertUtil;
import org.beifengtz.jvmm.web.entity.po.NodeConfPO;
import org.beifengtz.jvmm.web.entity.po.NodePO;

/**
 * Description: TODO
 *
 * Created in 11:23 2022/2/25
 *
 * @author beifengtz
 */
@Data
public class NodeDTO {
    private NodePO node;
    private NodeConfPO nodeConf;

    public void check() {
        AssertUtil.checkArguments(node != null, "Missing required param: node");
        AssertUtil.checkArguments(nodeConf != null, "Missing required param: nodeConf");
        AssertUtil.checkArguments(nodeConf.getId() == node.getId(), "Invalid argument: id");
        node.check();
        nodeConf.check();
    }
}
