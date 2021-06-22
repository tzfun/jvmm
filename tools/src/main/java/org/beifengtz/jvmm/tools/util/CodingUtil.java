package org.beifengtz.jvmm.tools.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 14:37 2021/5/22
 *
 * @author beifengtz
 */
public class CodingUtil {

    public static String encodeUrl(String url, String enc) {
        try {
            return URLEncoder.encode(url, enc);
        } catch (UnsupportedEncodingException ignored) {
            return url;
        }
    }

    public static String encodeUrl(String url) {
        return encodeUrl(url, "utf-8");
    }

    public static String decodeUrl(String url, String enc) {
        try {
            return URLDecoder.decode(url, enc);
        } catch (UnsupportedEncodingException ignored) {
            return url;
        }
    }

    public static String decodeUrl(String url) {
        return decodeUrl(url, "UTF-8");
    }

    /**
     * 将hex字符串转为byte数组
     *
     * @param src hex string
     * @return byte array
     */
    public static byte[] hexStr2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }

    /**
     * 将byte数组转为hex字符串
     *
     * @param b byte array
     * @return hex string
     */
    public static String bytes2HexStr(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte value : b) {
            sb.append(String.format("%02X", value));
        }
        return sb.toString();
    }
}
