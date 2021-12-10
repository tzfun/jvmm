package org.beifengtz.jvmm.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.convey.channel.JvmmChannelInitializer;
import org.beifengtz.jvmm.server.handler.ServerHandlerProvider;
import org.beifengtz.jvmm.server.logger.DefaultILoggerFactory;
import org.beifengtz.jvmm.tools.factory.LoggerFactory;
import org.beifengtz.jvmm.tools.logger.LoggerLevel;
import org.beifengtz.jvmm.tools.util.FileUtil;
import org.beifengtz.jvmm.tools.util.PidUtil;
import org.beifengtz.jvmm.tools.util.PlatformUtil;
import org.beifengtz.jvmm.tools.util.SystemPropertyUtil;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.BindException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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

    private Instrumentation instrumentation;
    private int rebindTimes = 0;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Thread shutdownHook;

    private ServerBootstrap(Instrumentation inst) {
        this.instrumentation = inst;
    }

    public static ServerBootstrap getInstance() {
        if (bootstrap == null) {
            throw new IllegalStateException("Server bootstrap has not been instantiated.");
        }
        return bootstrap;
    }

    public synchronized static ServerBootstrap getInstance(String args) throws Throwable {
        return getInstance(null, args);
    }

    public synchronized static ServerBootstrap getInstance(Instrumentation inst, String args) throws Throwable {
        if (bootstrap != null) {
            return bootstrap;
        }

        Configuration config = parseConfig(args);
        initLogger(config.getLogLevel());

        bootstrap = new ServerBootstrap(inst);
        ServerConfig.setConfiguration(config);

        return bootstrap;
    }

    private static void initLogger(String levelStr) {
        String lvl = levelStr.toUpperCase(Locale.ROOT);
        LoggerLevel level = LoggerLevel.INFO;

        switch (lvl) {
            case "WARN":
                level = LoggerLevel.WARN;
                break;
            case "DEBUG":
                level = LoggerLevel.DEBUG;
                break;
            case "ERROR":
                level = LoggerLevel.ERROR;
                break;
            case "OFF":
                level = LoggerLevel.OFF;
                break;
            case "TRACE":
                level = LoggerLevel.TRACE;
                break;
        }

//        java.util.logging.Logger.getGlobal().setLevel(l1);

        DefaultILoggerFactory defaultILoggerFactory = DefaultILoggerFactory.newInstance(level);

        InternalLoggerFactory.setDefaultFactory(defaultILoggerFactory);
        LoggerFactory.register(defaultILoggerFactory);
    }

    private static Logger logger() {
        return LoggerFactory.logger(ServerBootstrap.class);
    }

    /**
     * 解析attach传过来的参数
     *
     * 优先级：
     * 1. 如果参数中有配置属性优先取参数中的配置（即使 config 指定的配置文件中设有相同的属性，仍优先取参数中的值）；
     * 2. 如果参数中指定了 config 参数，（此参数指定向一个配置文件，格式可以是 yml 也可以是 properties），从配置中读取；
     * 3. 如果既没有指定配置文件又没有参数配置，则读默认配置，默认配置：{@link Configuration.Builder}。
     *
     * @param args 参数格式：name=jvmm_server;port.bind=5010;port.autoIncrease=true;http.maxChunkSize=52428800
     * @return {@link Configuration}
     */
    private static Configuration parseConfig(String args) {
        if (args == null) {
            args = "";
        }
        Map<String, String> argMap = new HashMap<>(4);
        String[] argArray = args.split(";");
        for (String arg : argArray) {
            String[] kv = arg.split("=");
            if (kv.length > 1) {
                String k = kv[0].trim();
                String v = kv[1].trim();
                argMap.put(k, v);
            }
        }
        Configuration.Builder cb = Configuration.newBuilder();
        String configPath = argMap.get("config");
        if (Objects.nonNull(configPath)) {
            File configFile = new File(configPath);
            if (!configFile.exists()) {
                throw new RuntimeException("Can not found config file from args: " + configPath);
            }

            try {
                String lowerCase = configPath.toLowerCase(Locale.ROOT);
                if (lowerCase.endsWith("yml") || lowerCase.endsWith("yaml")) {
                    ConfigFileMapping mapping = FileUtil.readYml(configFile.getAbsolutePath(), ConfigFileMapping.class);
                    cb.mergeFromMapping(mapping);
                } else if (lowerCase.endsWith("properties")) {
                    Map<String, String> propMap = FileUtil.readProperties(configFile.getAbsolutePath());
                    propMap.putAll(argMap);
                    argMap = propMap;
                }
            } catch (IOException e) {
                throw new RuntimeException("An error occurred while reading the file. " + e.getMessage(), e);
            }
        }

        cb.mergeFromProperties(argMap);

        return cb.build();
    }

    public void start() {
        if (ServerConfig.isInited()) {
            int realBindPort = ServerConfig.getRealBindPort();
            if (realBindPort < 0) {
                start(ServerConfig.getConfiguration().getPort());
            } else {
                logger().info("Jvmm server already started on {}", realBindPort);
            }
        } else {
            logger().error("Jvmm Server start failed, configuration not inited.");
        }
    }

    private void start(int bindPort) {
        if (PlatformUtil.portAvailable(bindPort)) {
            logger().info("Try to start jvmm server service. target port: {}", bindPort);
            rebindTimes++;
            final io.netty.bootstrap.ServerBootstrap b = new io.netty.bootstrap.ServerBootstrap();

            if (bossGroup == null) {
                bossGroup = JvmmChannelInitializer.newEventLoopGroup(1);
            }
            if (workerGroup == null) {
                workerGroup = JvmmChannelInitializer.newEventLoopGroup(ServerConfig.getWorkThread());
            }
            try {
                if (rebindTimes > BIND_LIMIT_TIMES) {
                    throw new BindException("The number of port monitoring retries exceeds the limit: " + BIND_LIMIT_TIMES);
                }

                ChannelFuture f = b.group(bossGroup, workerGroup)
                        .channel(JvmmChannelInitializer.serverChannelClass(bossGroup))
                        .childHandler(new JvmmChannelInitializer(new ServerHandlerProvider(10, workerGroup)))
                        .bind(bindPort).syncUninterruptibly();

                logger().info("Jvmm server service started on {}", bindPort);
                ServerConfig.setRealBindPort(bindPort);

                if (shutdownHook == null) {
                    shutdownHook = new Thread(this::stop);
                    shutdownHook.setName("jvmm-shutdown-hook");
                }
                Runtime.getRuntime().addShutdownHook(shutdownHook);
                f.channel().closeFuture().syncUninterruptibly();
            } catch (BindException e) {
                if (rebindTimes < BIND_LIMIT_TIMES && ServerConfig.getConfiguration().isAutoIncrease()) {
                    start(bindPort + 1);
                } else {
                    logger().error("Jvmm server start up failed. " + e.getMessage(), e);
                    stop();
                }
            } catch (Throwable e) {
                logger().error("Jvmm server start up failed. " + e.getMessage(), e);
                stop();
            }
        } else {
            logger().info("Port {} is not available.", bindPort);
            if (ServerConfig.getConfiguration().isAutoIncrease()) {
                start(bindPort + 1);
            }
        }
    }

    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully(0, 2, TimeUnit.SECONDS);
            bossGroup = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully(1, 2, TimeUnit.SECONDS);
            workerGroup = null;
        }
        ServerConfig.setRealBindPort(-1);
        if (shutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (Exception ignored) {
            }
            shutdownHook = null;
        }
        logger().info("Jvmm server service stopped.");

        try {
            Class<?> bootClazz = Thread.currentThread().getContextClassLoader().loadClass(AGENT_BOOT_CLASS);
            bootClazz.getMethod("serverStop").invoke(null);
        } catch (Throwable e) {
            logger().error("Invoke agent boot method(#serverStop) failed", e);
        }
    }

    public boolean serverAvailable() {
        return ServerConfig.getRealBindPort() >= 0;
    }

    /**
     * just for test
     */
    public static void main(String[] args) throws Throwable {
        int tp = 8090;
        String homePath = SystemPropertyUtil.get("user.dir").replaceAll("\\\\", "/");
        String agentJar = homePath + "/agent/build/libs/jvmm-agent.jar";
        String serverJar = homePath + "/server/build/libs/jvmm-server.jar";

        long pid = PidUtil.findProcessByPort(tp);
        if (pid > 0) {
            Configuration config = Configuration.newBuilder().setLogLevel("info").build();
            AttachProvider.getInstance().attachAgent(pid, agentJar, serverJar, config);
        } else {
            System.err.println("Can not found any program is listening port " + tp);
        }
    }
}
