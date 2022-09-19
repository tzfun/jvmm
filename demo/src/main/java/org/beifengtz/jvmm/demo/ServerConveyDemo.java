package org.beifengtz.jvmm.demo;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.logger.LoggerLevel;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Description: TODO
 * <p>
 * Created in 17:16 2021/12/15
 *
 * @author beifengtz
 */
public class ServerConveyDemo {
    public static void main(String[] args) throws InterruptedException {
        LoggerInitializer.init(LoggerLevel.INFO);
        Logger logger = LoggerFactory.logger(ServerConveyDemo.class);

        EventLoopGroup executor = ChannelInitializers.newEventLoopGroup(1);
        JvmmConnector connector = JvmmConnector.newInstance("127.0.0.1", 5010, executor, false, "jvmm_acc", "jvmm_pwd");
        Future<Boolean> f1 = connector.connect();
        if (f1.await(3, TimeUnit.SECONDS)) {
            if (f1.getNow()) {

                connector.registerCloseListener(() -> {
                    logger.info("Jvmm connector closed");
                    executor.shutdownGracefully();
                });

                connector.registerListener(response -> {
                    if (Objects.equals(response.getType(), GlobalType.JVMM_TYPE_PONG.name())) {
                        logger.info("pong");
                        connector.close();
                    }
                });

                connector.send(JvmmRequest.create().setType(GlobalType.JVMM_TYPE_PING));
            } else {
                logger.error("Authentication failed!");
            }
        } else {
            logger.error("Connect time out");
        }
    }
}
