package org.beifengtz.jvmm.demo;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Description: TODO
 * <p>
 * Created in 17:16 2021/12/15
 *
 * @author beifengtz
 */
public class ServerConveyDemo {

    private static Logger logger;

    public static void main(String[] args) throws Exception {
        logger = LoggerFactory.getLogger(ServerConveyDemo.class);

        EventLoopGroup executor = ChannelInitializers.newEventLoopGroup(1);

        JvmmRequest request = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_PING);

        //  向jvmm服务器发送一个消息并同步等待
        JvmmResponse response = sendMsgOnce(executor, request);

        // 向jvmm服务器发送一个消息，异步响应
        sendMsgOnceAsync(executor, request, System.out::println);

        //  得到一个保持活跃的连接器
        JvmmConnector connector = getKeepAliveConnector(executor);
        connector.send(request);
    }

    private static JvmmResponse sendMsgOnce(EventLoopGroup executor, JvmmRequest request) throws Exception {
        return JvmmConnector.waitForResponse(executor, "127.0.0.1:5010", request);
    }

    private static void sendMsgOnceAsync(EventLoopGroup executor, JvmmRequest request, JvmmConnector.MsgReceiveListener listener) throws Exception {
        JvmmConnector connector = JvmmConnector.newInstance("127.0.0.1", 5010, executor, false, "jvmm_acc", "jvmm_pwd");
        Future<Boolean> f1 = connector.connect();
        if (f1.await(3, TimeUnit.SECONDS)) {
            if (f1.getNow()) {

                connector.registerCloseListener(() -> {
                    logger.info("Jvmm connector closed");
                    executor.shutdownGracefully();
                });

                connector.registerListener(response -> {
                    listener.onMessage(response);
                    connector.close();
                });

                connector.send(request);
            } else {
                logger.error("Authentication failed!");
            }
        } else {
            logger.error("Connect time out");
        }
    }

    private static JvmmConnector getKeepAliveConnector(EventLoopGroup executor) throws Exception {
        JvmmConnector connector = JvmmConnector.newInstance("127.0.0.1", 5010, executor, true, "jvmm_acc", "jvmm_pwd");
        Future<Boolean> f1 = connector.connect();
        if (f1.await(3, TimeUnit.SECONDS)) {
            if (f1.getNow()) {

                connector.registerCloseListener(() -> {
                    logger.info("Jvmm connector closed");
                    executor.shutdownGracefully();
                });

                return connector;
            } else {
                logger.error("Authentication failed!");
            }
        } else {
            logger.error("Connect time out");
        }
        return null;
    }
}
