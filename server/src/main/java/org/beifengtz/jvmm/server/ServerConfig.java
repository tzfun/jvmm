package org.beifengtz.jvmm.server;

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

    private static volatile Configuration configuration;

    private static volatile int realBindPort = -1;

    public static synchronized void setConfiguration(Configuration config) {
        configuration = config;
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

}
