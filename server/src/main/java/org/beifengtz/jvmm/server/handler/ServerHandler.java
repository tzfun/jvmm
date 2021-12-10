package org.beifengtz.jvmm.server.handler;

import org.beifengtz.jvmm.convey.handler.JvmmRequestHandler;
import org.beifengtz.jvmm.tools.factory.LoggerFactory;
import org.slf4j.Logger;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 9:39 2021/5/18
 *
 * @author beifengtz
 */
public class ServerHandler extends JvmmRequestHandler {
    private static final Logger logger = LoggerFactory.logger(ServerHandler.class);

    @Override
    public Logger logger() {
        return logger;
    }
}
