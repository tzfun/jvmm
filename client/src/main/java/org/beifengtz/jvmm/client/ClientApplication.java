package org.beifengtz.jvmm.client;

import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.logger.LoggerLevel;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:34 上午 2021/12/11
 *
 * @author beifengtz
 */
public class ClientApplication {
    static {
        LoggerFactory.setDefaultLoggerLevel(LoggerLevel.DEBUG);
        InternalLoggerFactory.setDefaultFactory(new DefaultInternalLoggerFactory());
    }

    public static void main(String[] args) throws Throwable {
        Commander.parse(args);
//        Commander.parse(new String[]{
//                "-h","127.0.0.1:5010"
//        });
        System.exit(0);

//        int tp = 8090;
//        String homePath = SystemPropertyUtil.get("user.dir").replaceAll("\\\\", "/");
//        String agentJar = homePath + "/agent/build/libs/jvmm-agent.jar";
//        String serverJar = homePath + "/server/build/libs/jvmm-server.jar";
//
//        long pid = PidUtil.findProcessByPort(tp);
//        if (pid > 0) {
//            Configuration config = Configuration.newBuilder().setLogLevel("info").build();
//            AttachProvider.getInstance().attachAgent(pid, agentJar, serverJar, config);
//        } else {
//            System.err.println("Can not found any program is listening port " + tp);
//        }
    }
}
