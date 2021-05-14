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
}
