package org.beifengtz.jvmm.common.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(10000);
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
        conn.setConnectTimeout(3000);
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

    /**
     * 从url中获取参数键值对。支持两种数组读取方式：
     * <url>
     * <li>1. http://jvmm.beifengtz.com?arr=1&arr=2&arr=3</li>
     * <li>2. http://jvmm.beifengtz.com?arr[]=1&arr[]=2&arr[]=3</li>
     * </url>
     *
     * @param uri url地址
     * @return 参数键值对，key为 String，value为 String 或者 List
     * @throws UnsupportedEncodingException url解码失败时抛出此异常
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseParams(URI uri) throws UnsupportedEncodingException {
        String query = uri.getQuery();
        Map<String, Object> params = new HashMap<>();
        if (query == null) {
            return params;
        }
        String[] split = query.split("&");
        for (String str : split) {
            if (!str.contains("=")) {
                continue;
            }
            String[] kv = str.split("=");
            if (kv.length <= 1) {
                continue;
            }
            String key = URLDecoder.decode(kv[0], "UTF-8");
            String value = URLDecoder.decode(kv[1], "UTF-8");
            if (key.endsWith("[]")) {
                List<String> array = (List<String>) params.computeIfAbsent(key.substring(0, key.length() - 2), o -> new ArrayList<>());
                array.add(value);
            } else {
                if (params.containsKey(key)) {
                    Object presentValue = params.get(key);
                    List<String> array;
                    if (presentValue instanceof List) {
                        array = (List<String>) presentValue;
                    } else {
                        array = new ArrayList<>();
                        array.add((String) presentValue);
                    }
                    array.add(value);
                    params.put(key, array);
                } else {
                    params.put(key, value);
                }
            }
        }
        return params;
    }
}
