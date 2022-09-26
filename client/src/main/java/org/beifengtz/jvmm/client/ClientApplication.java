package org.beifengtz.jvmm.client;

import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.logger.LoggerLevel;
import org.beifengtz.jvmm.convey.DefaultInternalLoggerFactory;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:34 上午 2021/12/11
 *
 * @author beifengtz
 */
public class ClientApplication {
    static {
        LoggerFactory.setDefaultLoggerLevel(LoggerLevel.INFO);
        InternalLoggerFactory.setDefaultFactory(DefaultInternalLoggerFactory.newInstance(LoggerLevel.INFO));
    }

    public static void main(String[] args) throws Throwable {
        CommandRunner.run(args);
    }
}
