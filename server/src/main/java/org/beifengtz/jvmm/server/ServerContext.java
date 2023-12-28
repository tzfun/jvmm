package org.beifengtz.jvmm.server;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.factory.ExecutorFactory;
import org.beifengtz.jvmm.common.util.ClassLoaderUtil;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.common.util.IOUtil;
import org.beifengtz.jvmm.common.util.SystemPropertyUtil;
import org.beifengtz.jvmm.convey.channel.ChannelUtil;
import org.beifengtz.jvmm.server.entity.conf.Configuration;
import org.beifengtz.jvmm.server.enums.ServerType;
import org.beifengtz.jvmm.server.service.JvmmService;

import java.io.File;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
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

    private static final Map<ServerType, JvmmService> serviceContainer = new ConcurrentHashMap<>(1);

    private static volatile Configuration configuration;

    private static volatile EventLoopGroup workerGroup;

    private static volatile boolean loadedLogLib = false;

    private static boolean fromAgent = false;

    private static Instrumentation instrumentation;

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

            System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_HOME, homePath.getAbsolutePath());
            System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_TEMP_PATH, tempPath.getAbsolutePath());
        } catch (Exception e) {
            InternalLoggerFactory.getInstance(ServerContext.class).error("Init server config failed: " + e.getMessage(), e);
        }
    }

    public static void setFromAgent(boolean flag) {
        fromAgent = flag;
    }

    public static boolean isFromAgent() {
        return fromAgent;
    }

    public static void setInstrumentation(Instrumentation inst) {
        instrumentation = inst;
    }

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public static synchronized void setConfiguration(Configuration config) {
        configuration = config;
        config.getLog().setSystemProperties();
        System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_WORK_THREAD, String.valueOf(config.getWorkThread()));
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static boolean isInitialized() {
        return configuration != null;
    }

    public static String getTempPath() {
        return System.getProperty(SystemPropertyUtil.PROPERTY_JVMM_TEMP_PATH, ".jvmm");
    }

    public static String getHomePath() {
        return System.getProperty(SystemPropertyUtil.PROPERTY_JVMM_HOME, "");
    }

    public static Set<ServerType> getServerSet() {
        return serviceContainer.keySet();
    }

    public static EventLoopGroup getWorkerGroup() {
        if (workerGroup == null) {
            synchronized (ServerContext.class) {
                if (workerGroup == null) {
                    workerGroup = ChannelUtil.newEventLoopGroup(1, ExecutorFactory.getThreadPool());
                }
            }
        }
        return workerGroup;
    }

    /**
     * 关闭指定某一个服务
     *
     * @param type {@link ServerType}
     * @return true-服务已成功关闭  false-服务未启动
     */
    public static synchronized boolean stop(ServerType type) {
        JvmmService service = serviceContainer.get(type);
        if (service != null) {
            service.shutdown();
            return true;
        }
        return false;
    }

    /**
     * 关闭所有服务，如果有agent向agent通知服务关闭
     */
    public static synchronized void stopAll() {
        for (ServerType server : ServerType.values()) {
            try {
                ServerContext.stop(server);
            } catch (Exception e) {
                InternalLoggerFactory.getInstance(ServerContext.class).error("An exception occurred while shutting down the jvmm service: " + e.getMessage(), e);
            }
        }

        if (fromAgent) {
            try {
                Class<?> bootClazz = Thread.currentThread().getContextClassLoader().loadClass("org.beifengtz.jvmm.agent.AgentBootStrap");
                bootClazz.getMethod("serverStop").invoke(null);
            } catch (Throwable e) {
                InternalLoggerFactory.getInstance(ServerContext.class).error("Invoke agent boot method(#serverStop) failed", e);
            }
        }
        instrumentation = null;
    }

    public static void startIfAbsent(ServerType serverType, JvmmService service, Promise<Integer> promise) {
        try {
            if (serviceContainer.containsKey(serverType)) {
                promise.trySuccess(serviceContainer.get(serverType).getPort());
            } else {
                service.start(promise);
            }
        } catch (Exception e) {
            promise.tryFailure(e);
            if (ServerContext.isBootApp()) {
                System.exit(1);
            }
        }
    }

    public static JvmmService getService(ServerType type) {
        return serviceContainer.get(type);
    }

    public static synchronized void registerService(ServerType type, JvmmService service) {
        serviceContainer.put(type, service);
    }

    public static synchronized void unregisterService(ServerType type) {
        serviceContainer.remove(type);
        if (serviceContainer.isEmpty()) {
            ExecutorFactory.releaseThreadPool();
            workerGroup.shutdownGracefully();
            workerGroup = null;
            if (isBootApp()) {
                System.exit(0);
            }
        }
    }

    static synchronized void loadLoggerLib() throws Throwable {
        if (loadedLogLib) {
            return;
        }

        try {
            Class<?> clazz = Class.forName("org.beifengtz.jvmm.log.JvmmLoggerFactory");
            InternalLoggerFactory instance = (InternalLoggerFactory) clazz.getMethod("getInstance").invoke(null);
            InternalLoggerFactory.setDefaultFactory(instance);
        } catch (NoClassDefFoundError | ClassNotFoundException ignored) {
        }

        try {
            Class.forName("org.slf4j.impl.StaticLoggerBinder");
            InternalLoggerFactory.getInstance(ServerContext.class).info("The SLF4J implementation already exists in the Jvmm startup environment, this log framework is used by default");
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            final String jarName = "jvmm-logger.jar";
            InputStream is = ServerApplication.class.getResourceAsStream("/" + jarName);
            if (is == null) {
                throw new RuntimeException("Can not load jvmm logger library, case: jar not found");
            }
            File file = new File(FileUtil.getTempPath(), jarName);
            FileUtil.writeByteArrayToFile(file, IOUtil.toByteArray(is));

            ClassLoaderUtil.loadJar(ServerContext.class.getClassLoader(), file.toPath().toUri().toURL());
            InternalLoggerFactory.getInstance(ServerContext.class).info("Using jvmm logger framework as the implementation of SLF4J");
        }

        loadedLogLib = true;
    }

    public static boolean isBootApp() {
        return SystemPropertyUtil.getBoolean(SystemPropertyUtil.PROPERTY_JVMM_SERVER_APPLICATION, false);
    }
}
