package org.beifengtz.jvmm.server.exporter;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.convey.socket.HttpUtil;
import org.beifengtz.jvmm.server.entity.conf.AuthOptionConf;
import org.beifengtz.jvmm.server.entity.conf.SentinelSubscriberConf;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * description: TODO
 * date: 15:49 2024/1/25
 *
 * @author beifengtz
 */
public class PrometheusExporter implements JvmmExporter<byte[], byte[]> {

    @Override
    public CompletableFuture<byte[]> export(SentinelSubscriberConf subscriber, byte[] data) {
        String url = subscriber.getUrl() + "/api/v1/write";
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/x-protobuf");
        headers.put("X-Prometheus-Remote-Write-Version", "0.1.0");
        headers.put("Content-Encoding", "snappy");
        AuthOptionConf auth = subscriber.getAuth();
        if (auth != null) {
            String authorization = Base64.getUrlEncoder().encodeToString((auth.getUsername() + ":" + auth.getPassword()).getBytes());
            headers.put("Authorization", "Basic " + authorization);
        }
        return HttpUtil.asyncReq(HttpUtil.packRequest(HttpMethod.POST, url, data, headers));
    }
}
