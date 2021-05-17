package org.beifengtz.jvmm.client;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.beifengtz.jvmm.client.channel.ClientLogicSocketChannel;
import org.beifengtz.jvmm.convey.channel.StringChannelInitializer;
import org.beifengtz.jvmm.tools.util.FileUtil;
import org.beifengtz.jvmm.tools.util.PlatformUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:11 2021/5/22
 *
 * @author beifengtz
 */
public class ClientBootstrap {
    private static final Logger log = LoggerFactory.getLogger(ClientBootstrap.class);

    private static volatile ClientBootstrap clientBootstrap;
    private final Configuration config;
    private int port = -1;

    private ClientBootstrap() {
        this(parseConfig(null));
    }

    private ClientBootstrap(Configuration config) {
        this.config = config;
        start(config.getPort());
    }

    public synchronized static ClientBootstrap getInstance(String args) throws Throwable {
        return getInstance(null, args);
    }

    public synchronized static ClientBootstrap getInstance(Instrumentation inst, String args) throws Throwable {
        if (clientBootstrap != null) {
            return clientBootstrap;
        }
        clientBootstrap = new ClientBootstrap(parseConfig(args));
        return clientBootstrap;
    }

    /**
     * 解析attach传过来的参数
     *
     * @param args 参数格式：name=jvmm_client;port.bind=5010;port.autoIncrease=true;http.maxChunkSize=52428800
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
            String k = kv[0].trim();
            String v = kv[1].trim();
            argMap.put(k, v);
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
                    cb.merge(mapping);
                } else if (lowerCase.endsWith("properties")) {
                    Map<String, String> propMap = FileUtil.readProperties(configFile.getAbsolutePath());
                    propMap.putAll(argMap);
                    argMap = propMap;
                }
            } catch (IOException e) {
                throw new RuntimeException("An error occurred while reading the file. " + e.getMessage(), e);
            }

        }

        String name = argMap.get("name");
        if (Objects.nonNull(name)) {
            cb.setName(name);
        }

        String portBind = argMap.get("port.bind");
        if (Objects.nonNull(portBind)) {
            cb.setPort(Integer.parseInt(portBind));
        }

        String portAutoIncrease = argMap.get("port.autoIncrease");
        if (Objects.nonNull(portAutoIncrease)) {
            cb.setAutoIncrease(Boolean.parseBoolean(portAutoIncrease));
        }

        return cb.build();
    }

    public int getPort() {
        return port;
    }

    private void start(int bindPort) {
        if (PlatformUtil.portAvailable(bindPort)) {
            log.info("Try to start client service. bind:{}", bindPort);

            final ServerBootstrap b = new ServerBootstrap();
            final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            final EventLoopGroup workerGroup = new NioEventLoopGroup();

            b.group(bossGroup, workerGroup)
                    .channel(ClientLogicSocketChannel.class)
                    .childHandler(new StringChannelInitializer(null));
        } else {
            log.info("Port {} is not available.", bindPort);
            start(bindPort + 1);
        }
    }
}
