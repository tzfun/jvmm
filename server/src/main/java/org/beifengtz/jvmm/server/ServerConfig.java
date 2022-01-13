package org.beifengtz.jvmm.server;

import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.core.conf.Configuration;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Paths;

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

    private static final Logger logger = LoggerFactory.logger(ServerConfig.class);

    public static final String STATUS_OK = "ok";

    private static volatile Configuration configuration;

    private static volatile int realBindPort = -1;

    static {
        try {
            File home = Paths.get(ServerConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile();
            File homePath = home.isDirectory() ? home.getParentFile() : home.getParentFile().getParentFile();

            //	兼容IDEA
            if (homePath.getAbsolutePath().endsWith("classes")) {
                homePath = homePath.getParentFile().getParentFile();
                home = home.getParentFile().getParentFile();
            }
            // 兼容gradle 5.2 eclipse
            if (homePath.getAbsolutePath().trim().endsWith("bin")) {
                homePath = homePath.getParentFile();
                home = home.getParentFile();
            }
            if (homePath.getAbsolutePath().trim().endsWith("java")) {
                homePath = homePath.getParentFile().getParentFile().getParentFile();
                home = home.getParentFile().getParentFile().getParentFile();
            }

            File tempPath = home.isDirectory()
                    ? new File(home.getParent(), "temp")
                    : new File(home.getParentFile().getParent(), "temp");

            System.setProperty("jvmm.home", homePath.getAbsolutePath());
            System.setProperty("jvmm.tempPath", tempPath.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Init server config failed: " + e.getMessage(), e);
        }
    }

    public static synchronized void setConfiguration(Configuration config) {
        configuration = config;
        System.setProperty("jvmm.log.level", config.getLogLevel());
        System.setProperty("jvmm.workThread", String.valueOf(config.getWorkThread()));
    }

    public static synchronized void setRealBindPort(int port) {
        realBindPort = port;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static boolean isInited() {
        return configuration != null;
    }

    public static int getRealBindPort() {
        return realBindPort;
    }

    public static int getWorkThread() {
        return configuration.getWorkThread();
    }

    public static String getTempPath() {
        return System.getProperty("jvmm.tempPath", "");
    }

    public static String getHomePath() {
        return System.getProperty("jvmm.home", "");
    }
}
