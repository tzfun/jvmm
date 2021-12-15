package org.beifengtz.jvmm.demo;

import org.beifengtz.jvmm.core.conf.Configuration;
import org.beifengtz.jvmm.server.ServerBootstrap;

/**
 * Description: TODO
 *
 * Created in 17:04 2021/12/15
 *
 * @author beifengtz
 */
public class ServerBootDemo {
    public static void main(String[] args) throws Throwable {
        Configuration config = Configuration.newBuilder()
                .setName("jvmm_server_test")
                .setPort(5010)
                .setAutoIncrease(true)
                .setHttpMaxChunkSize(52428800)
                .setLogLevel("info")
                .setLogUseJvmm(true)
                .setSecurityEnable(true)
                .setSecurityAccount("jvmm_acc")
                .setSecurityPassword("jvmm_pwd")
                .setWorkThread(2)
                .build();
        ServerBootstrap server = ServerBootstrap.getInstance(config);
        server.start();

        Thread.sleep(3000);

        server.stop();
    }
}
