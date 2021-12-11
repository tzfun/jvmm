package org.beifengtz.jvmm.client;

import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.logger.LoggerLevel;

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
        LoggerFactory.setDefaultLoggerLevel(LoggerLevel.DEBUG);
    }

    public static void main(String[] args) throws Throwable {
        Commander.parse(args);
    }
}
