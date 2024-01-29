package org.beifengtz.jvmm.server.exporter;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.server.entity.conf.SentinelSubscriberConf;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

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
        try {
            URL requestUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);

            conn.setRequestProperty("Content-type", "application/x-protobuf");
            conn.setRequestProperty("X-Prometheus-Remote-Write-Version", "0.1.0");

            String authorization = Base64.getUrlEncoder().encodeToString(("username" + ":" + "password").getBytes());
            conn.setRequestProperty("Authorization", "Basic " + authorization);

            try {
                OutputStream os = conn.getOutputStream();
                os.write(data);
                os.flush();
                os.close();
                conn.connect();
            } finally {
                conn.disconnect();
            }
        } catch (Throwable e) {
            logger.error("Write to prometheus failed: " + url, e);
        }
    }
}
