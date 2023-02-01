package org.beifengtz.jvmm.server;

import org.beifengtz.jvmm.server.entity.conf.Configuration;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    public static void main(String[] args) throws Exception {
        System.setErr(new PrintStream(Files.newOutputStream(Paths.get("jvmm-err.log"))));
        ServerBootstrap server = ServerBootstrap.getInstance(loadConf(args));
        server.start();
    }
}
