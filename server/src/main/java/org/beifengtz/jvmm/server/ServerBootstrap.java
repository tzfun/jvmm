package org.beifengtz.jvmm.server;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.logger.LoggerLevel;
import org.beifengtz.jvmm.convey.DefaultInternalLoggerFactory;
import org.beifengtz.jvmm.server.entity.conf.Configuration;
import org.beifengtz.jvmm.server.entity.conf.ServerConf;
import org.beifengtz.jvmm.server.enums.ServerType;
import org.beifengtz.jvmm.server.logger.DefaultILoggerFactory;
import org.beifengtz.jvmm.server.logger.DefaultJvmmILoggerFactory;
import org.beifengtz.jvmm.server.service.JvmmHttpServerService;
import org.beifengtz.jvmm.server.service.JvmmSentinelService;
import org.beifengtz.jvmm.server.service.JvmmServerService;
import org.beifengtz.jvmm.server.service.JvmmService;
import org.beifengtz.jvmm.server.service.ServiceManager;
import org.slf4j.Logger;

import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:11 2021/5/22
 *
 * @author beifengtz
 */
public class ServerBootstrap {

    private static final int BIND_LIMIT_TIMES = 20;
    public static final String AGENT_BOOT_CLASS = "org.beifengtz.jvmm.agent.AgentBootStrap";
    private static volatile ServerBootstrap bootstrap;
    private static boolean fromAgent;

    private Instrumentation instrumentation;
    private Thread shutdownHook;
    private volatile ServiceManager serviceManager;

    private ServerBootstrap(Instrumentation inst) {
        this.instrumentation = inst;
    }

    public static ServerBootstrap getInstance() {
        return getInstance(null, (Configuration) null);
    }

    public synchronized static ServerBootstrap getInstance(Configuration config) {
        return getInstance(null, config);
    }

    public synchronized static ServerBootstrap getInstance(Instrumentation inst, String args) {
        String[] argKv = args.split(";");
        String configFileUrl = null;
        for (String s : argKv) {
            String[] split = s.split("=");
            if (split.length > 1) {
                if ("config".equalsIgnoreCase(split[0])) {
                    configFileUrl = split[1];
                } else if ("fromAgent".equalsIgnoreCase(split[0])) {
                    fromAgent = Boolean.parseBoolean(split[1]);
                }
            }
        }

        return getInstance(inst, configFileUrl == null ? null : Configuration.parseFromUrl(configFileUrl));
    }

    public synchronized static ServerBootstrap getInstance(Instrumentation inst, Configuration config) {
        if (bootstrap != null) {
            if (config != null) {
                ServerContext.setConfiguration(config);
            }
            return bootstrap;
        }

        if (config == null) {
            config = new Configuration();
        }

        if (config.getLog().isUseJvmm()) {
            initJvmmLogger(config.getLog().getLevel());
        } else {
            if (fromAgent) {
                initAgentLogger(config.getLog().getLevel());
            } else {
                initBaseLogger();
            }
        }

        bootstrap = new ServerBootstrap(inst);
        ServerContext.setConfiguration(config);

        return bootstrap;
    }

    private static void initJvmmLogger(String levelStr) {
        LoggerLevel level = LoggerLevel.valueOf(levelStr.toUpperCase(Locale.ROOT));
        InternalLoggerFactory.setDefaultFactory(DefaultInternalLoggerFactory.newInstance(level));
        LoggerFactory.register(DefaultJvmmILoggerFactory.newInstance(level));
    }

    private static void initBaseLogger() {
        LoggerFactory.register(org.slf4j.LoggerFactory.getILoggerFactory());
    }

    private static void initAgentLogger(String levelStr) {
        LoggerLevel level = LoggerLevel.valueOf(levelStr.toUpperCase(Locale.ROOT));

        DefaultILoggerFactory defaultILoggerFactory = DefaultILoggerFactory.newInstance(level);

        InternalLoggerFactory.setDefaultFactory(defaultILoggerFactory);
        LoggerFactory.register(defaultILoggerFactory);
    }

    private static Logger logger() {
        return LoggerFactory.logger(ServerBootstrap.class);
    }

    public void start(Function<Object, Object> callback) {
        if (ServerContext.isInited()) {
            StringBuffer sb = new StringBuffer();

            //  如果服务类型不同，先关闭现有服务
            Set<ServerType> serverSet = ServerContext.getServerSet();
            ServerConf serverConf = ServerContext.getConfiguration().getServer();
            String[] split = serverConf.getType().split(",");

            Set<ServerType> argServers = new HashSet<>(split.length);
            Set<ServerType> startServers = new HashSet<>();
            Set<ServerType> stopServers = new HashSet<>();

            for (String t : split) {
                ServerType server = ServerType.of(t);
                argServers.add(server);
                if (!serverSet.contains(server)) {
                    startServers.add(server);
                } else {
                    sb.append("ok:").append(server).append(":").append(ServerContext.getService(server).getPort()).append(";");
                }
            }

            for (ServerType server : serverSet) {
                if (!argServers.contains(server)) {
                    stopServers.add(server);
                }
            }

            for (ServerType server : stopServers) {
                try {
                    ServerContext.stop(server);
                } catch (Exception e) {
                    logger().error("An exception occurred while shutting down the jvmm service: " + e.getMessage(), e);
                }
            }

            CountDownLatch latch = new CountDownLatch(startServers.size());

            if (serviceManager == null) {
                serviceManager = new ServiceManager();
            }

            for (ServerType server : startServers) {
                JvmmService service = null;
                Promise<Integer> promise = new DefaultPromise<>(ServerContext.getBoosGroup().next());
                promise.addListener((GenericFutureListener<Future<Integer>>) future -> {
                    latch.countDown();
                    if (future.isSuccess()) {
                        sb.append("ok:").append(server).append(":").append(future.get()).append(";");
                    } else {
                        sb.append(server).append(":").append(future.cause().getMessage()).append(";");
                    }
                });

                if (server == ServerType.jvmm) {
                    service = new JvmmServerService();
                } else if (server == ServerType.http) {
                    service = new JvmmHttpServerService();
                } else if (server == ServerType.sentinel) {
                    service = new JvmmSentinelService();
                }
                assert service != null;
                serviceManager.startIfAbsent(service, promise);
            }

            if (shutdownHook == null) {
                shutdownHook = new Thread(this::stop);
                shutdownHook.setName("jvmm-shutdown");
                Runtime.getRuntime().addShutdownHook(shutdownHook);
            }

            try {
                if (latch.await(15, TimeUnit.SECONDS)) {
                    callback.apply(sb.toString());
                } else {
                    String msg = "A service timed out for unknown reasons";
                    logger().error(msg);
                    callback.apply(sb.append(msg).toString());
                }
            } catch (InterruptedException e) {
                String msg = "Failed to wait for service to start: " + e.getMessage();
                logger().error(msg, e);
                callback.apply(sb.append(msg).toString());
            }
        } else {
            String msg = "Jvmm Server start failed, configuration not inited.";
            callback.apply(msg);
            logger().error(msg);
        }
    }

    public void stop() {
        for (ServerType server : ServerType.values()) {
            try {
                ServerContext.stop(server);
            } catch (Exception e) {
                logger().error("An exception occurred while shutting down the jvmm service: " + e.getMessage(), e);
            }
        }
        ServerContext.getBoosGroup().shutdownGracefully();
        if (shutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (Exception ignored) {
            }
            shutdownHook = null;
        }
        logger().info("Jvmm server service stopped.");

        if (fromAgent) {
            try {
                Class<?> bootClazz = Thread.currentThread().getContextClassLoader().loadClass(AGENT_BOOT_CLASS);
                bootClazz.getMethod("serverStop").invoke(null);
            } catch (Throwable e) {
                logger().error("Invoke agent boot method(#serverStop) failed", e);
            }
        }
    }
}
