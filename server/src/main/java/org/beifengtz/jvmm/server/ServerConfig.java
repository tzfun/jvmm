package org.beifengtz.jvmm.server;

import org.beifengtz.jvmm.core.conf.Configuration;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 14:26 2021/5/22
 *
 * @author beifengtz
 */
public class ServerConfig {

    public static final String STATUS_OK = "ok";

    private static volatile Configuration configuration;

    private static volatile int realBindPort = -1;

    public static synchronized void setConfiguration(Configuration config) {
        configuration = config;
        System.setProperty("jvmm.log.level", config.getLogLevel());
        System.setProperty("jvmm.workThread", String.valueOf(config.getWorkThread()));
    }

    public static synchronized void setRealBindPort(int port) {
        realBindPort = port;
    }

    public static Configuration getConfiguration(){
        return configuration;
    }

    public static boolean isInited(){
        return configuration != null;
    }

    public static int getRealBindPort(){
        return realBindPort;
    }

    public static int getWorkThread() {
        return configuration.getWorkThread();
    }

}
