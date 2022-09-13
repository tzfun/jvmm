package org.beifengtz.jvmm.server.controller;

import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.JvmmController;
import org.beifengtz.jvmm.convey.annotation.JvmmMapping;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.server.ServerBootstrap;

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
    public void shutdownServer() {
        try {
            ServerBootstrap bootstrap = ServerBootstrap.getInstance();
            bootstrap.stop();
        } catch (IllegalStateException ignored) {
        }
    }
}
