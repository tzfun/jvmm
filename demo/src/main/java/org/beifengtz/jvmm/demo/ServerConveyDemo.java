package org.beifengtz.jvmm.demo;

import io.netty.channel.EventLoopGroup;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.exception.JvmmConnectFailedException;
import org.beifengtz.jvmm.convey.channel.ChannelUtil;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.enums.RpcType;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Description: TODO
 * <p>
 * Created in 17:16 2021/12/15
 *
 * @author beifengtz
 */
public class ServerConveyDemo {

    private static InternalLogger logger;

    public static void main(String[] args) throws Exception {
        logger = InternalLoggerFactory.getInstance(ServerConveyDemo.class);

        EventLoopGroup executor = ChannelUtil.newEventLoopGroup(1);

        JvmmRequest request = JvmmRequest.create().setType(RpcType.JVMM_PING);

        //  向jvmm服务器发送一个消息并同步等待
        JvmmResponse response = sendMsgOnce(executor, request);

        // 向jvmm服务器发送一个消息，异步响应
        sendMsgOnceAsync(executor, request, System.out::println);

        //  得到一个保持活跃的连接器
        JvmmConnector connector = getKeepAliveConnector(executor);
        connector.send(request);
    }

    private static JvmmResponse sendMsgOnce(EventLoopGroup executor, JvmmRequest request) throws Exception {
        return JvmmConnector.waitForResponse(executor, "127.0.0.1", 5010, request, 5, TimeUnit.SECONDS);
    }

    private static void sendMsgOnceAsync(EventLoopGroup executor, JvmmRequest request,
                                         JvmmConnector.MsgReceiveListener listener) {
        JvmmConnector connector = JvmmConnector.newInstance("127.0.0.1", 5010, executor, false, "jvmm_acc", "jvmm_pwd");
        CompletableFuture<Boolean> connectFuture = connector.connect();
        connectFuture.whenComplete((success, throwable) -> {
            if (throwable == null) {
                if (success) {
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
        });
    }

    private static JvmmConnector getKeepAliveConnector(EventLoopGroup executor) throws Exception {
        JvmmConnector connector = JvmmConnector.newInstance("127.0.0.1", 5010, executor, true, "jvmm_acc", "jvmm_pwd");
        CompletableFuture<Boolean> connectFuture = connector.connect();
        Boolean success = connectFuture.get(3, TimeUnit.SECONDS);
        if (success) {
            connector.registerCloseListener(() -> {
                logger.info("Jvmm connector closed");
                executor.shutdownGracefully();
            });

            return connector;
        } else {
            throw new JvmmConnectFailedException("Authentication failed!");
        }
    }
}
