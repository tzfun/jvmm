package org.beifengtz.jvmm.web;

import io.netty.channel.nio.NioEventLoopGroup;
import org.beifengtz.jvmm.convey.GlobalType;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.socket.JvmmSocketConnector;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class WebApplicationTests {

    @Test
    void contextLoads() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();
        JvmmSocketConnector client = JvmmSocketConnector.newInstance("127.0.0.1", 5010, true, group);

        CountDownLatch latch = new CountDownLatch(5);
        client.registerListener((response) -> {
            System.out.println("==> " + response.toJsonStr());
            if (response.getType().equals(GlobalType.JVMM_TYPE_TIMER_COLLECT_MEMORY_INFO.name())) {
                latch.countDown();
            }
        });
        if (client.connect().await(3, TimeUnit.SECONDS)) {
            System.out.println(client.ping());
            client.send(JvmmRequest.create().setType(GlobalType.JVMM_TYPE_COLLECT_SYSTEM_STATIC_INFO));
            client.send(JvmmRequest.create().setType(GlobalType.JVMM_TYPE_COLLECT_PROCESS_INFO));
            client.send(JvmmRequest.create().setType(GlobalType.JVMM_TYPE_COLLECT_THREAD_DYNAMIC_INFO));
            client.send(JvmmRequest.create().setType(GlobalType.JVMM_TYPE_COLLECT_MEMORY_INFO));

            client.send(JvmmRequest.create().setType(GlobalType.JVMM_TYPE_TIMER_COLLECT_MEMORY_INFO));

            latch.await();

            client.send(JvmmRequest.create().setType(GlobalType.JVMM_TYPE_STOP_TIMER_COLLECT_MEMORY_INFO));
            client.close();
            group.shutdownGracefully();
        }
    }

}
