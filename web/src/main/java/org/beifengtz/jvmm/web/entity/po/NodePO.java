package org.beifengtz.jvmm.web.entity.po;

import com.baomidou.mybatisplus.annotations.TableName;
import io.netty.util.NetUtil;
import lombok.Data;
import org.beifengtz.jvmm.common.util.AssertUtil;

/**
 * Description: TODO
 *
 * Created in 11:16 2022/2/25
 *
 * @author beifengtz
 */
@Data
@TableName("node_t")
public class NodePO {
    private int id;
    private String name;
    private String ip;
    private Integer port;
    private boolean authEnable;
    private String authName;
    private String authPass;
    private long createTime;
    private long updateTime;

    public void check() {
        AssertUtil.checkArguments(getName() != null, "Invalid argument: name.");
        AssertUtil.checkArguments(NetUtil.isValidIpV4Address(getIp()), "Invalid argument: ip.");
        AssertUtil.checkArguments(getPort() != null, "Invalid argument: port.");
        if (isAuthEnable()) {
            AssertUtil.checkArguments(getAuthName() != null, "Invalid auth name with authentication enabled.");
            AssertUtil.checkArguments(getAuthPass() != null, "Invalid auth password with authentication enabled.");
        }
    }
}
