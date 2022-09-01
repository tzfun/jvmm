package org.beifengtz.jvmm.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.logger.LoggerLevel;
import org.beifengtz.jvmm.common.util.PlatformUtil;
import org.beifengtz.jvmm.convey.DefaultInternalLoggerFactory;
import org.beifengtz.jvmm.convey.channel.JvmmChannelInitializer;
import org.beifengtz.jvmm.core.conf.ConfigParser;
import org.beifengtz.jvmm.core.conf.Configuration;
import org.beifengtz.jvmm.server.handler.ServerHandlerProvider;
import org.beifengtz.jvmm.server.logger.DefaultILoggerFactory;
import org.beifengtz.jvmm.server.logger.DefaultJvmmILoggerFactory;
import org.slf4j.Logger;

import java.lang.instrument.Instrumentation;
import java.net.BindException;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
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

    private Instrumentation instrumentation;
    private int rebindTimes = 0;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Thread shutdownHook;

    private ServerBootstrap(Instrumentation inst) {
        this.instrumentation = inst;
    }

    public static ServerBootstrap getInstance() {
        return getInstance(null, Configuration.defaultInstance());
    }

    public synchronized static ServerBootstrap getInstance(String args) {
        return getInstance(null, args);
    }

    public synchronized static ServerBootstrap getInstance(Configuration config) {
        return getInstance(null, config);
    }

    public synchronized static ServerBootstrap getInstance(Instrumentation inst, String args) {
        if (bootstrap != null) {
            return bootstrap;
        }
        System.out.println(args);
        return getInstance(inst, ConfigParser.parseFromArgs(args));
    }

    public synchronized static ServerBootstrap getInstance(Instrumentation inst, Configuration config) {
        if (bootstrap != null) {
            return bootstrap;
        }

        if (config.isLogUseJvmm()) {
            initJvmmLogger(config.getLogLevel());
        } else {
            if (config.isFromAgent()) {
                initAgentLogger(config.getLogLevel());
            } else {
                initBaseLogger();
            }
        }

        bootstrap = new ServerBootstrap(inst);
        ServerConfig.setConfiguration(config);

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

    @SuppressWarnings("unchecked")
    public void start(Function callback) {
        if (ServerConfig.isInited()) {
            int realBindPort = ServerConfig.getRealBindPort();
            if (realBindPort < 0) {
                start(ServerConfig.getConfiguration().getPort(), callback);
            } else {
                callback.apply(realBindPort);
                logger().info("Jvmm server already started on {}", realBindPort);
            }
        } else {
            String msg = "Jvmm Server start failed, configuration not inited.";
            callback.apply(msg);
            logger().error(msg);
        }
    }

    @SuppressWarnings("unchecked")
    private void start(int bindPort, Function callback) {
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
                callback.apply(bindPort);

                if (shutdownHook == null) {
                    shutdownHook = new Thread(this::stop);
                    shutdownHook.setName("jvmm-shutdown-hook");
                }
                Runtime.getRuntime().addShutdownHook(shutdownHook);
                f.channel().closeFuture().syncUninterruptibly();
            } catch (BindException e) {
                if (rebindTimes < BIND_LIMIT_TIMES && ServerConfig.getConfiguration().isAutoIncrease()) {
                    start(bindPort + 1, callback);
                } else {
                    logger().error("Jvmm server start up failed. " + e.getMessage(), e);
                    stop();
                }
            } catch (Throwable e) {
                logger().error("Jvmm server start up failed. " + e.getMessage(), e);
                callback.apply(e.getMessage());
                stop();
            }
        } else {
            logger().info("Port {} is not available, auto increase:{}", bindPort, ServerConfig.getConfiguration().isAutoIncrease());
            if (ServerConfig.getConfiguration().isAutoIncrease()) {
                start(bindPort + 1, callback);
            } else {
                callback.apply("Port " + bindPort + " is not available and the auto increase switch is closed.");
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

    public int bindPort() {
        return ServerConfig.getRealBindPort();
    }
}
