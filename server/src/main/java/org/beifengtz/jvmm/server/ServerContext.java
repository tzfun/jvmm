package org.beifengtz.jvmm.server;

import io.netty.channel.EventLoopGroup;
import org.beifengtz.jvmm.common.util.ClassLoaderUtil;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.common.util.IOUtil;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.server.entity.conf.Configuration;
import org.beifengtz.jvmm.server.enums.ServerType;
import org.beifengtz.jvmm.server.service.JvmmService;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
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

    public static final String STATUS_OK = "ok";

    private static volatile Configuration configuration;

    private static final Map<ServerType, JvmmService> serviceContainer = new ConcurrentHashMap<>(1);

    private static volatile EventLoopGroup boosGroup;

    private static volatile boolean loadedLogLib = false;

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
            LoggerFactory.getLogger(ServerContext.class).error("Init server config failed: " + e.getMessage(), e);
        }
    }

    public static synchronized void setConfiguration(Configuration config) {
        configuration = config;
        config.getLog().setSystemProperties();
        System.setProperty("jvmm.workThread", String.valueOf(config.getWorkThread()));
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static boolean isInited() {
        return configuration != null;
    }

    public static String getTempPath() {
        return System.getProperty("jvmm.tempPath", ".jvmm");
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
                    boosGroup = ChannelInitializers.newEventLoopGroup(2);
                }
            }
        }
        return boosGroup;
    }

    /**
     * 关闭指定某一个服务
     *
     * @param type {@link ServerType}
     * @return true-服务已成功关闭  false-服务未启动
     */
    public static boolean stop(ServerType type) {
        JvmmService service = serviceContainer.get(type);
        if (service != null) {
            service.shutdown();
            serviceContainer.remove(type);
            return true;
        }
        return false;
    }

    /**
     * 关闭所有服务
     */
    public static void stopAll() {
        ServerBootstrap.getInstance().stop();
    }

    public static JvmmService getService(ServerType type) {
        return serviceContainer.get(type);
    }

    public static void registerService(ServerType type, JvmmService service) {
        serviceContainer.put(type, service);
    }

    public static void unregisterService(ServerType type) {
        serviceContainer.remove(type);
    }

    static synchronized void loadLoggerLib() throws Throwable {
        if (loadedLogLib) {
            return;
        }

        try {
            Class.forName("org.slf4j.impl.StaticLoggerBinder");
            LoggerFactory.getLogger(ServerContext.class).info("The SLF4J implementation already exists in the Jvmm startup environment, this log framework is used by default");
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            final String jarName = "jvmm-logger.jar";
            InputStream is = ServerApplication.class.getResourceAsStream("/" + jarName);
            if (is == null) {
                throw new RuntimeException("Can not load jvmm logger library, case: jar not found");
            }
            File file = new File(JvmmFactory.getTempPath(), jarName);
            FileUtil.writeByteArrayToFile(file, IOUtil.toByteArray(is));

            ClassLoaderUtil.loadJar(ServerContext.class.getClassLoader(), file.toPath().toUri().toURL());
            LoggerFactory.getLogger(ServerContext.class).info("Using jvmm logger framework as the implementation of SLF4J");
        }

        loadedLogLib = true;
    }
}
