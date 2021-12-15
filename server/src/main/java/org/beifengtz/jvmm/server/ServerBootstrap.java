package org.beifengtz.jvmm.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.logger.LoggerLevel;
import org.beifengtz.jvmm.common.util.PlatformUtil;
import org.beifengtz.jvmm.convey.channel.JvmmChannelInitializer;
import org.beifengtz.jvmm.core.conf.ConfigParser;
import org.beifengtz.jvmm.core.conf.Configuration;
import org.beifengtz.jvmm.server.handler.ServerHandlerProvider;
import org.beifengtz.jvmm.server.logger.DefaultILoggerFactory;
import org.slf4j.Logger;

import java.lang.instrument.Instrumentation;
import java.net.BindException;
import java.util.Locale;
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

        return getInstance(inst, ConfigParser.parseFromArgs(args));
    }

    public synchronized static ServerBootstrap getInstance(Instrumentation inst, Configuration config) throws Throwable {
        if (bootstrap != null) {
            return bootstrap;
        }
        if (config.isFromAgent()) {
            initAgentLogger(config.getLogLevel());
        } else {
            initBaseLogger();
        }

        bootstrap = new ServerBootstrap(inst);
        ServerConfig.setConfiguration(config);

        return bootstrap;
    }

    private static void initBaseLogger() {
        LoggerFactory.register(org.slf4j.LoggerFactory.getILoggerFactory());
    }

    private static void initAgentLogger(String levelStr) {
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

        DefaultILoggerFactory defaultILoggerFactory = DefaultILoggerFactory.newInstance(level);

        InternalLoggerFactory.setDefaultFactory(defaultILoggerFactory);
        LoggerFactory.register(defaultILoggerFactory);
    }

    private static Logger logger() {
        return LoggerFactory.logger(ServerBootstrap.class);
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

                logger().info("Jvmm server service started on {}, node name: {}", bindPort, ServerConfig.getConfiguration().getName());
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
}
