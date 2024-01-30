package org.beifengtz.jvmm.server.exporter;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.convey.socket.HttpUtil;
import org.beifengtz.jvmm.server.entity.conf.SentinelSubscriberConf;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * description: TODO
 * date: 15:49 2024/1/25
 *
 * @author beifengtz
 */
public class PrometheusExporter implements JvmmExporter<byte[]> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(PrometheusExporter.class);

    @Override
    public void export(SentinelSubscriberConf subscriber, byte[] data) {
        String url = subscriber.getUrl() + "/api/v1/write";
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/x-protobuf");
        headers.put("X-Prometheus-Remote-Write-Version", "0.1.0");
        String authorization = Base64.getUrlEncoder().encodeToString(("username" + ":" + "password").getBytes());
        headers.put("Authorization", "Basic " + authorization);

        HttpUtil.asyncReq(HttpUtil.packRequest(HttpMethod.POST, url, data, headers)).whenComplete(((bytes, throwable) -> {
            if (throwable == null) {
                System.out.println(new String(bytes, StandardCharsets.UTF_8));
            } else {
                logger.warn("Post prometheus failed. {}: {}", throwable.getClass(), throwable.getMessage());
                throwable.printStackTrace();
            }
        }));
    }
}
