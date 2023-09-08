package org.beifengtz.jvmm.server.controller;

import org.beifengtz.jvmm.common.util.AssertUtil;
import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.JvmmController;
import org.beifengtz.jvmm.convey.annotation.JvmmMapping;
import org.beifengtz.jvmm.convey.annotation.RequestParam;
import org.beifengtz.jvmm.convey.enums.RpcType;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.enums.ServerType;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:58 下午 2021/5/31
 *
 * @author beifengtz
 */
@JvmmController
@HttpController
public class ServerController {

    @JvmmMapping(RpcType.JVMM_HEARTBEAT)
    public void heartbeat() {
    }

    @JvmmMapping(RpcType.JVMM_SERVER_SHUTDOWN)
    @HttpRequest("/server/shutdown")
    public String shutdown(@RequestParam String target) {
        AssertUtil.checkArguments(target != null, "Missing required param 'target'");
        ServerType type = ServerType.of(target);
        if (ServerContext.stop(type)) {
            return "ok";
        } else {
            return "Service not running";
        }
    }
}
