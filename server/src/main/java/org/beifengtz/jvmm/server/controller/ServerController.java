package org.beifengtz.jvmm.server.controller;

import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.JvmmController;
import org.beifengtz.jvmm.convey.annotation.JvmmMapping;
import org.beifengtz.jvmm.convey.annotation.RequestParam;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.enums.ServerType;

import static com.google.gson.internal.$Gson$Preconditions.checkArgument;

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

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_HEARTBEAT)
    public void heartbeat() {
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_SERVER_SHUTDOWN)
    @HttpRequest("/server/shutdown")
    public void shutdown(@RequestParam String target) {
        checkArgument(target != null);
        ServerType type = ServerType.of(target);
        ServerContext.stop(type);
    }
}
