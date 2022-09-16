package org.beifengtz.jvmm.server;

import io.netty.channel.EventLoopGroup;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.server.entity.conf.Configuration;
import org.beifengtz.jvmm.server.enums.ServerType;
import org.beifengtz.jvmm.server.service.JvmmService;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 14:26 2021/5/22
 *
 * @author beifengtz
 */
public class ServerContext {

    private static final Logger logger = LoggerFactory.logger(ServerContext.class);

    public static final String STATUS_OK = "ok";

    private static volatile Configuration configuration;

    private static final Map<ServerType, JvmmService> serviceContainer = new ConcurrentHashMap<>(1);

    private static volatile EventLoopGroup boosGroup;

    static {
        try {
            File home = Paths.get(ServerContext.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile();
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
        System.setProperty("jvmm.log.level", config.getLog().getLevel());
        System.setProperty("jvmm.workThread", String.valueOf(config.getWorkThread()));
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static boolean isInited() {
        return configuration != null;
    }

    public static String getTempPath() {
        return System.getProperty("jvmm.tempPath", "tmp");
    }

    public static String getHomePath() {
        return System.getProperty("jvmm.home", "");
    }

    public static Set<ServerType> getServerSet() {
        return serviceContainer.keySet();
    }

    public static EventLoopGroup getBoosGroup() {
        if (boosGroup == null || boosGroup.isShutdown()) {
            synchronized (ServerContext.class) {
                if (boosGroup == null || boosGroup.isShutdown()) {
                    boosGroup = ChannelInitializers.newEventLoopGroup(1);
                }
            }
        }
        return boosGroup;
    }

    public static void stop(ServerType type) {
        JvmmService service = serviceContainer.get(type);
        if (service != null) {
            service.stop();
            serviceContainer.remove(type);
        }
    }

    public static JvmmService getService(ServerType type) {
        return serviceContainer.get(type);
    }

    public static void registerService(ServerType type, JvmmService service) {
        serviceContainer.put(type, service);
    }
}
