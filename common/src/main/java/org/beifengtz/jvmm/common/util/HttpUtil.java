package org.beifengtz.jvmm.common.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 15:01 2022/9/14
 *
 * @author beifengtz
 */
public class HttpUtil {

    public static byte[] getBytes(String url) throws IOException {
        URL requestUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
        conn.setRequestMethod("GET");
        try {
            return IOUtil.toByteArray(conn.getInputStream());
        } finally {
            conn.disconnect();
        }
    }

    public static String get(String url) throws IOException {
        return new String(getBytes(url), StandardCharsets.UTF_8);
    }

    public static String post(String url, String data) throws IOException {
        return post(url, data, null);
    }

    public static String post(String url, String data, Map<String, String> heads) throws IOException {
        URL requestUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        if (heads != null)
            heads.forEach(conn::setRequestProperty);

        try {
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            writer.write(data);
            writer.flush();
            tryClose(writer);
            tryClose(os);
            conn.connect();

            return IOUtil.toString(conn.getInputStream());
        } finally {
            conn.disconnect();
        }
    }

    private static void tryClose(Closeable os) {
        if (null != os) {
            IOException ie = IOUtil.close(os);
            if (ie != null) {
                ie.printStackTrace();
            }
        }
    }
}
