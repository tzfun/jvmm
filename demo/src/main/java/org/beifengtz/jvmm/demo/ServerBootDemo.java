package org.beifengtz.jvmm.demo;

import org.beifengtz.jvmm.common.logger.LoggerLevel;
import org.beifengtz.jvmm.common.util.IPUtil;
import org.beifengtz.jvmm.server.ServerBootstrap;
import org.beifengtz.jvmm.server.entity.conf.AuthOptionConf;
import org.beifengtz.jvmm.server.entity.conf.Configuration;
import org.beifengtz.jvmm.server.entity.conf.HttpServerConf;
import org.beifengtz.jvmm.server.entity.conf.JvmmServerConf;
import org.beifengtz.jvmm.server.entity.conf.LogConf;
import org.beifengtz.jvmm.server.entity.conf.SentinelConf;
import org.beifengtz.jvmm.server.entity.conf.SentinelSubscriberConf;
import org.beifengtz.jvmm.server.entity.conf.ServerConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: TODO
 * <p>
 * Created in 17:04 2021/12/15
 *
 * @author beifengtz
 */
public class ServerBootDemo {
    public static void main(String[] args) throws Throwable {
        ServerBootstrap server = ServerBootstrap.getInstance();
        server.start(msg -> transformServerCallback(msg.toString()));
    }

    private static Configuration constructConfig() {
        AuthOptionConf globalAuth = new AuthOptionConf()
                .setEnable(true)
                .setUsername("jvmm-acc")
                .setPassword("jvmm-pass");

        JvmmServerConf jvmmServer = new JvmmServerConf()
                .setPort(5010)
                .setAdaptivePort(true)
                .setAuth(globalAuth);

        HttpServerConf httpServer = new HttpServerConf()
                .setPort(8080)
                .setAdaptivePort(true)
                .setAuth(globalAuth);

        SentinelConf sentinel = new SentinelConf()
                .addSubscriber(new SentinelSubscriberConf().setUrl("http://exaple.jvmm.com/subscriber"))
                .addSubscriber(new SentinelSubscriberConf().setUrl("http://127.0.0.1:8080/subscriber")
                        .setAuth(new AuthOptionConf().setEnable(true)
                                .setUsername("auth-account")
                                .setPassword("auth-password")))
                .setInterval(10)
                .setCount(-1);

        return new Configuration()
                .setName("jvmm-server")
                .setWorkThread(2)
                .setLog(new LogConf().setLevel(LoggerLevel.INFO))
                .setServer(new ServerConf()
                        .setType("jvmm,sentinel")
                        .setJvmm(jvmmServer)
                        .setHttp(httpServer)
                        .addSentinel(sentinel));
    }

    private static Object transformServerCallback(String content) {
        Logger logger = LoggerFactory.getLogger(ServerBootDemo.class);
        if ("start".equals(content)) {
            logger.info("Try to start or stop services...");
        } else if (content.startsWith("info:")) {
            logger.info(content.substring(content.indexOf(":") + 1));
        } else if (content.startsWith("warn:")) {
            logger.warn(content.substring(content.indexOf(":") + 1));
        } else if (content.startsWith("ok:")) {
            String[] split = content.split(":");
            if ("new".equals(split[1])) {
                if ("sentinel".equals(split[2])) {
                    logger.info("New service started => [sentinel]");
                } else {
                    logger.info("New service started on {}:{} => [{}]", IPUtil.getLocalIP(), split[3], split[2]);
                }
            } else if ("ready".equals(split[1])) {
                if ("sentinel".equals(split[2])) {
                    logger.info("Service already started => [sentinel]");
                } else {
                    logger.info("Service already started on {}:{} => [{}]", IPUtil.getLocalIP(), split[3], split[2]);
                }
            } else {
                logger.info("==> {}", content);
            }
        } else if ("end".equals(content)) {
            logger.info("Server boot finished");
        } else {
            System.out.println(content);
        }
        return null;
    }
}
