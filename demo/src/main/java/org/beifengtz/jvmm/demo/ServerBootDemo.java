package org.beifengtz.jvmm.demo;

import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.logger.LoggerLevel;
import org.beifengtz.jvmm.server.ServerBootstrap;
import org.beifengtz.jvmm.server.entity.conf.Configuration;
import org.slf4j.Logger;

import java.io.InputStream;

/**
 * Description: TODO
 * <p>
 * Created in 17:04 2021/12/15
 *
 * @author beifengtz
 */
public class ServerBootDemo {
    public static void main(String[] args) throws Throwable {
        LoggerInitializer.init(LoggerLevel.INFO);
        Logger logger = LoggerFactory.logger(ServerBootDemo.class);

        InputStream is = ServerBootDemo.class.getResourceAsStream("/config.yml");
        if (is != null) {
            //  从resource中读取配置文件，除此之外你也可以自己通过代码构造Configuration
            Configuration config = Configuration.parseFromStream(is);
            ServerBootstrap server = ServerBootstrap.getInstance(config);
            server.start(msg -> transformServerCallback(msg.toString()));
        } else {
            logger.error("Can not found config.yml in resources");
        }
    }

    private static Object transformServerCallback(String content) {
        Logger logger = LoggerFactory.logger(ServerBootDemo.class);
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
                    logger.info("New service started: [sentinel]");
                } else {
                    logger.info("New service started on {}: [{}]", split[3], split[2]);
                }
            } else if ("ready".equals(split[1])) {
                if ("sentinel".equals(split[2])) {
                    logger.info("Service already started: [sentinel]");
                } else {
                    logger.info("Service already started on {}: [{}]", split[3], split[2]);
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
