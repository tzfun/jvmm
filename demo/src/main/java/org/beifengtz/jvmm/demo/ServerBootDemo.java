package org.beifengtz.jvmm.demo;

import org.beifengtz.jvmm.server.ServerBootstrap;
import org.beifengtz.jvmm.server.entity.conf.Configuration;

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
        InputStream is = ServerBootDemo.class.getResourceAsStream("/config.yml");
        if (is != null) {
            Configuration config = Configuration.parseFromStream(is);
            ServerBootstrap server = ServerBootstrap.getInstance(config);
            server.start(msg -> {
                System.out.println("Server started " + msg);
                return null;
            });
        } else {
            System.err.println("Can not found config.yml in resources");
        }
    }
}
