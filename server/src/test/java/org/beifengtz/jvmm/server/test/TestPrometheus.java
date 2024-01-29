package org.beifengtz.jvmm.server.test;

import org.beifengtz.jvmm.core.contanstant.CollectionType;
import org.beifengtz.jvmm.core.entity.JvmmData;
import org.beifengtz.jvmm.server.entity.conf.SentinelSubscriberConf;
import org.beifengtz.jvmm.server.entity.conf.SentinelSubscriberConf.SubscriberType;
import org.beifengtz.jvmm.server.exporter.PrometheusExporter;
import org.beifengtz.jvmm.server.prometheus.PrometheusUtil;
import org.beifengtz.jvmm.server.service.JvmmService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * description: TODO
 * date: 18:12 2024/1/29
 *
 * @author beifengtz
 */
public class TestPrometheus {
    @Test
    public void testRemoteWrite() throws Exception {
        SentinelSubscriberConf subscriber = new SentinelSubscriberConf()
                .setType(SubscriberType.prometheus)
                .setUrl("http://192.168.0.161:9090");

        PrometheusExporter exporter = new PrometheusExporter();
        List<CollectionType> tasks = Arrays.asList(CollectionType.values());
        CountDownLatch cdl = new CountDownLatch(1);
        JvmmService.collectByOptions(tasks, Arrays.asList(3306, 6379, 8080), null, pair -> {
            if (pair.getLeft().get() <= 0) {
                JvmmData data = pair.getRight().setNode("test_node");
                byte[] dataBytes = PrometheusUtil.pack(data);
                exporter.export(subscriber, dataBytes);
                System.out.println("Exported " + dataBytes.length);
                cdl.countDown();
            }
        });

        cdl.await();
    }
}
