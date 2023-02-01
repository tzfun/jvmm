package org.beifengtz.jvmm.server;

import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.server.entity.conf.Configuration;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

/**
 * @author beifengtz
 * @description: TODO
 * @date 15:36 2023/2/1
 */
public class ServerApplication {

    private static Configuration loadConf(String[] args) {
        if (args.length > 0) {
            File file = new File(args[0]);
            if (file.exists()) {
                return Configuration.parseFromUrl(args[0]);
            } else {
                System.err.println("Config file not exists: " + args[0]);
                System.exit(-1);
            }
        } else {
            InputStream is = ServerApplication.class.getResourceAsStream("/config.yml");
            if (is == null) {
                File file = new File(System.getProperty("user.dir") + "/config.yml");
                if (file.exists()) {
                    return Configuration.parseFromUrl(file.getAbsolutePath());
                }
            } else {
                return Configuration.parseFromStream(is);
            }
        }
        return null;
    }

    private static Function<Object, Object> callbackHandler() {
        Logger logger = LoggerFactory.logger(ServerApplication.class);
        long start = System.currentTimeMillis();
        return msg -> {
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
                logger.info("Jvmm server boot finished in " + (System.currentTimeMillis() - start) + " ms");
            } else {
                System.out.println(content);
            }
            return null;
        };
    }

    public static void main(String[] args) throws Exception {
        System.setErr(new PrintStream(Files.newOutputStream(Paths.get("jvmm-err.log"))));
        ServerBootstrap server = ServerBootstrap.getInstance(loadConf(args));
        server.start(callbackHandler());
    }
}
