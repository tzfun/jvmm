package org.beifengtz.jvmm.server;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.logger.LoggerLevel;
import org.beifengtz.jvmm.common.util.IPUtil;
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

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
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
        if (configFileUrl == null) {
            System.err.println("No config file, about to use default configuration");
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

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    /**
     * 启动Server，使用默认的callback处理逻辑
     */
    public void start() {
        Logger logger = LoggerFactory.logger(ServerApplication.class);
        long start = System.currentTimeMillis();
        Function<Object, Object> callback = msg -> {
            String content = msg.toString();
            if ("start".equals(content)) {
                logger.info("Start jvmm services ...");
            } else if (content.startsWith("info:")) {
                logger.info(content.substring(content.indexOf(":") + 1));
            } else if (content.startsWith("warn:")) {
                logger.warn(content.substring(content.indexOf(":") + 1));
            } else if (content.startsWith("ok:")) {
                String[] split = content.split(":");
                if ("new".equals(split[1])) {
                    if ("sentinel".equals(split[2])) {
                        logger.info("New service started: [sentinel]");
                    } else {
                        logger.info("New service started on {}:{} => [{}]", IPUtil.getLocalIP(), split[3], split[2]);
                    }
                } else if ("ready".equals(split[1])) {
                    if ("sentinel".equals(split[2])) {
                        logger.info("Service already started: [sentinel]");
                    } else {
                        logger.info("Service already started on {} => [{}]", split[3], split[2]);
                    }
                } else {
                    logger.info(content);
                }
            } else if ("end".equals(content)) {
                logger.info("Jvmm server boot finished in " + (System.currentTimeMillis() - start) + " ms");
            } else {
                logger.info(content);
            }
            return null;
        };
        start(callback);
    }

    /**
     * 启动Server，启动信息会由callback回传
     *
     * @param callback 启动信息，包含日志信息
     */
    public void start(Function<Object, Object> callback) {
        callback.apply("start");
        try {
            if (ServerContext.isInited()) {

                //  如果服务类型不同，先关闭现有服务
                Set<ServerType> serverSet = ServerContext.getServerSet();
                ServerConf serverConf = ServerContext.getConfiguration().getServer();
                String[] split = serverConf.getType().split(",");

                Set<ServerType> argServers = new HashSet<>(split.length);
                Set<ServerType> startServers = new HashSet<>();
                Set<ServerType> stopServers = new HashSet<>();

                for (String t : split) {
                    ServerType server = ServerType.of(t);
                    if (server == ServerType.none) {
                        continue;
                    }
                    argServers.add(server);
                    if (!serverSet.contains(server)) {
                        startServers.add(server);
                    } else {
                        callback.apply("ok:ready:" + server + ":" + ServerContext.getService(server).getPort());
                    }
                }

                for (ServerType server : serverSet) {
                    if (!argServers.contains(server)) {
                        stopServers.add(server);
                    }
                }

                for (ServerType server : stopServers) {
                    try {
                        ServerContext.getService(server).addShutdownListener(() -> callback.apply("info:Service stopped: [" + server + "]"));
                        ServerContext.stop(server);
                    } catch (Exception e) {
                        logger().error("An exception occurred while shutting down the jvmm service: " + e.getMessage(), e);
                    }
                }

                if (startServers.size() == 0) {
                    callback.apply("warn:No new jvmm service need start");
                    callback.apply("end");
                    return;
                }

                if (serviceManager == null) {
                    serviceManager = new ServiceManager();
                }
                CountDownLatch latch = new CountDownLatch(startServers.size());
                for (ServerType server : startServers) {
                    JvmmService service = null;

                    if (server == ServerType.jvmm) {
                        service = new JvmmServerService();
                    } else if (server == ServerType.http) {
                        service = new JvmmHttpServerService();
                    } else if (server == ServerType.sentinel) {
                        service = new JvmmSentinelService();
                    }
                    assert service != null;

                    Promise<Integer> promise = new DefaultPromise<>(ServerContext.getBoosGroup().next());
                    JvmmService finalService = service;

                    service.addShutdownListener(() -> {
                        serviceManager.remove(finalService);
                        ServerContext.unregisterService(server);
                    });

                    promise.addListener((GenericFutureListener<Future<Integer>>) future -> {
                        try {
                            if (future.isSuccess()) {
                                ServerContext.registerService(server, finalService);
                                callback.apply("ok:new:" + server + ":" + future.get());
                            } else {
                                callback.apply(server + ":" + future.cause().getMessage());
                            }
                        } finally {
                            latch.countDown();
                        }
                    });
                    serviceManager.startIfAbsent(service, promise);
                }

                if (shutdownHook == null) {
                    shutdownHook = new Thread(this::stop);
                    shutdownHook.setName("jvmm-shutdown");
                    Runtime.getRuntime().addShutdownHook(shutdownHook);
                }

                try {
                    if (!latch.await(15, TimeUnit.SECONDS)) {
                        String msg = "A service timed out for unknown reasons";
                        logger().error(msg);
                        callback.apply(msg);
                    }
                } catch (InterruptedException e) {
                    String msg = "Failed to wait for service to start: " + e.getMessage();
                    logger().error(msg, e);
                    callback.apply(msg);
                } finally {
                    callback.apply("end");
                }
            } else {
                String msg = "Jvmm Server start failed, configuration not inited.";
                callback.apply(msg);
                callback.apply("end");
                logger().error(msg);
            }
        } catch (Throwable e) {
            logger().error("Jvmm service start failed: " + e.getMessage(), e);
            callback.apply(e.getMessage());
            callback.apply("end");
        }
    }

    /**
     * 关闭所有服务，如果有agent向agent通知服务关闭
     */
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

    public boolean redefineClass(ClassDefinition... definitions) throws ClassNotFoundException, UnmodifiableClassException {
        if (instrumentation == null) {
            return false;
        }
        instrumentation.redefineClasses(definitions);
        return true;
    }
}
