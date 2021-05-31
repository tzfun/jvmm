package org.beifengtz.jvmm.server.controller;

import org.beifengtz.jvmm.convey.GlobalType;
import org.beifengtz.jvmm.server.ServerBootstrap;
import org.beifengtz.jvmm.server.annotation.JvmmController;
import org.beifengtz.jvmm.server.annotation.JvmmMapping;

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
public class ServerController {

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_HEARTBEAT)
    public void heartbeat(){
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_SERVER_SHUTDOWN)
    public void shutdownServer() {
        try {
            ServerBootstrap bootstrap = ServerBootstrap.getInstance();
            bootstrap.stop();
        } catch (IllegalStateException ignored) {
        }
    }
}
