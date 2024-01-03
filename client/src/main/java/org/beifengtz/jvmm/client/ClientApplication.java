package org.beifengtz.jvmm.client;

import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.util.SystemPropertyUtil;
import org.beifengtz.jvmm.log.JvmmLoggerFactory;

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

    private static void initLogger() {
        System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_LOG_LEVEL, "info");
//        System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_LOG_PATTERN, "[%ansi{%level}{ERROR=31,INFO=32,WARN=33,DEBUG=34,TRACE=35}] %msg");
        System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_LOG_PATTERN, "%msg");
        InternalLoggerFactory.setDefaultFactory(JvmmLoggerFactory.getInstance());
    }

    public static void main(String[] args) throws Throwable {
        initLogger();
        CommandRunner.run(args);
//        CommandRunner.run(new String[]{"-m", "jar", "-e", "logger"});
//        CommandRunner.run(new String[]{"-h"});
    }
}
