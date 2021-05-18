package org.beifengtz.jvmm.server.handler;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.convey.handler.JvmmRequestHandler;

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
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ServerHandler.class);

    @Override
    public InternalLogger logger() {
        return logger;
    }
}
