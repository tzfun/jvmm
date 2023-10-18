package org.beifengtz.jvmm.web;

import io.netty.channel.EventLoopGroup;
import org.beifengtz.jvmm.convey.channel.ChannelUtil;
import org.beifengtz.jvmm.convey.enums.RpcType;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class WebApplicationTests {

    @Test
    void contextLoads() throws Exception {
        EventLoopGroup group = ChannelUtil.newEventLoopGroup(1);
        JvmmConnector client = JvmmConnector.newInstance("127.0.0.1", 5010, group, true);

        CountDownLatch latch = new CountDownLatch(5);
        client.registerListener((response) -> {
            System.out.println("==> " + response.toJsonStr());
            if (response.getType().equals(RpcType.JVMM_COLLECT_JVM_MEMORY_INFO)) {
                latch.countDown();
            }
        });
        if (client.connect().get(3, TimeUnit.SECONDS)) {
            System.out.println(client.ping());
            client.send(JvmmRequest.create().setType(RpcType.JVMM_COLLECT_SYS_INFO));
            client.send(JvmmRequest.create().setType(RpcType.JVMM_COLLECT_PROCESS_INFO));
            client.send(JvmmRequest.create().setType(RpcType.JVMM_COLLECT_JVM_THREAD_INFO));
            client.send(JvmmRequest.create().setType(RpcType.JVMM_COLLECT_JVM_MEMORY_INFO));

            client.send(JvmmRequest.create().setType(RpcType.JVMM_COLLECT_JVM_MEMORY_INFO));

            latch.await();

            client.send(JvmmRequest.create().setType(RpcType.JVMM_COLLECT_JVM_MEMORY_INFO));
            client.close();
            group.shutdownGracefully();
        } else {
            System.err.println("Failed to connect target server");
        }
    }

}
