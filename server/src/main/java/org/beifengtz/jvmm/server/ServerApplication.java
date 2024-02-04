package org.beifengtz.jvmm.server;

import org.beifengtz.jvmm.common.util.SystemPropertyUtil;
import org.beifengtz.jvmm.server.entity.conf.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * description: TODO
 * date 15:36 2023/2/1
 *
 * @author beifengtz
 */
public class ServerApplication {

    private static Configuration loadConf(String[] args) throws IOException {
        System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_SERVER_APPLICATION, "true");
        if (args.length > 0) {
            File file = new File(args[0]);
            if (file.exists()) {
                return Configuration.parseFromUrl(args[0]);
            } else {
                System.err.println("Config file not exists: " + args[0]);
                System.exit(1);
            }
        } else {
            File file = new File(System.getProperty("user.dir") + "/jvmm.yml");
            if (file.exists()) {
                return Configuration.parseFromYamlFile(file);
            } else {
                file = new File(System.getProperty("user.dir") + "/config/jvmm.yml");
                if (file.exists()) {
                    return Configuration.parseFromYamlFile(file);
                } else {
                    InputStream is = ServerApplication.class.getResourceAsStream("/jvmm.yml");
                    if (is == null) {
                        throw new RuntimeException("Can not found any configuration.");
                    } else {
                        return Configuration.parseFromStream(is);
                    }
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        ServerBootstrap.getInstance(loadConf(new String[]{"E:\\Project\\jvmm-dev\\jvmm-dev\\jvmm.yml"})).start(ServerBootstrap.class);
//        ServerBootstrap.getInstance(loadConf(args)).start(ServerBootstrap.class);
    }
}
