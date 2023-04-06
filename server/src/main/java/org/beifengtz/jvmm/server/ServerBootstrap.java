package org.beifengtz.jvmm.server;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.beifengtz.jvmm.common.util.IPUtil;
import org.beifengtz.jvmm.server.entity.conf.Configuration;
import org.beifengtz.jvmm.server.entity.conf.ServerConf;
import org.beifengtz.jvmm.server.enums.ServerType;
import org.beifengtz.jvmm.server.service.JvmmHttpServerService;
import org.beifengtz.jvmm.server.service.JvmmSentinelService;
import org.beifengtz.jvmm.server.service.JvmmServerService;
import org.beifengtz.jvmm.server.service.JvmmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashSet;
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

    private static volatile ServerBootstrap bootstrap;

    private ServerBootstrap(Instrumentation inst) {
        ServerContext.setInstrumentation(inst);
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
                    ServerContext.setFromAgent(Boolean.parseBoolean(split[1]));
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
        ServerContext.setConfiguration(config);

        if (ServerContext.isFromAgent()) {
            //  如果从agent启动，则取消默认的标注输出和文件输出
            System.setProperty("jvmm.log.printers", "agentProxy");
        }

        try {
            ServerContext.loadLoggerLib();
        } catch (Throwable t) {
            System.err.println("The implementation of SLF4J was not found in the startup environment, and the Jvmm log dependency failed to load:" + t.getMessage());
            t.printStackTrace();
        }

        bootstrap = new ServerBootstrap(inst);

        return bootstrap;
    }

    private static Logger logger() {
        return LoggerFactory.getLogger(ServerBootstrap.class);
    }

    /**
     * 启动Server，使用默认的callback处理逻辑
     */
    public void start() {
        Logger logger = LoggerFactory.getLogger(ServerApplication.class);
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
            if (ServerContext.isInitialized()) {

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

                    Promise<Integer> promise = new DefaultPromise<>(ServerContext.getWorkerGroup().next());
                    JvmmService finalService = service;

                    service.addShutdownListener(() -> ServerContext.unregisterService(server));

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
                    ServerContext.startIfAbsent(server, service, promise);
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

    public boolean redefineClass(ClassDefinition... definitions) throws ClassNotFoundException, UnmodifiableClassException {
        if (ServerContext.getInstrumentation() == null) {
            return false;
        }
        ServerContext.getInstrumentation().redefineClasses(definitions);
        return true;
    }
}
