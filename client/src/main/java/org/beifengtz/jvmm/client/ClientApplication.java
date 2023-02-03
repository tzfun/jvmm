package org.beifengtz.jvmm.client;

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
        System.setProperty("jvmm.log.level", "info");
        System.setProperty("jvmm.log.pattern", "[%ansi{%level}{ERROR=31,INFO=32,WARN=33,DEBUG=34,TRACE=35}] %msg");
    }

    public static void main(String[] args) throws Throwable {
        initLogger();
        CommandRunner.run(args);
    }
}
