package org.beifengtz.jvmm.demo;

import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.logger.LoggerLevel;
import org.beifengtz.jvmm.convey.DefaultInternalLoggerFactory;
import org.beifengtz.jvmm.server.logger.DefaultJvmmILoggerFactory;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:14 2022/9/19
 *
 * @author beifengtz
 */
public class LoggerInitializer {
    public static void init(LoggerLevel level) {
        InternalLoggerFactory.setDefaultFactory(DefaultInternalLoggerFactory.newInstance(level));
        LoggerFactory.register(DefaultJvmmILoggerFactory.newInstance(level));
    }
}
