package org.beifengtz.jvmm.tools.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:30 2021/05/11
 *
 * @author beifengtz
 */
public class StringUtil {

    /**
     * 内网ip过滤列表
     */
    private static final List<Pattern> innerIpFilter = new ArrayList<>();
    private static final Pattern hostPattern = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})((:\\d+)?)(/?)");

    static {
        Set<String> ipFilter = new HashSet<String>();
        ipFilter.add("^10\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])"
                + "\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])" + "\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])$");
        // B类地址范围: 172.16.0.0---172.31.255.255
        ipFilter.add("^172\\.(1[6789]|2[0-9]|3[01])\\" + ".(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])\\"
                + ".(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])$");
        // C类地址范围: 192.168.0.0---192.168.255.255
        ipFilter.add("^192\\.168\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])\\"
                + ".(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[0-9])$");
        ipFilter.add("127.0.0.1");
        ipFilter.add("0.0.0.0");
        ipFilter.add("localhost");
        for (String tmp : ipFilter) {
            innerIpFilter.add(Pattern.compile(tmp));
        }
    }

    public static String genUUID(boolean upper) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        if (upper) {
            return uuid.toUpperCase();
        } else {
            return uuid.toLowerCase();
        }
    }

    public static String emptyOrDefault(String target, String def) {
        return Objects.isNull(target) || target.trim().isEmpty() ? def : target;
    }

    public static boolean isEmpty(String str) {
        return Objects.isNull(str) || str.isEmpty();
    }

    public static boolean isDomainUrl(String source) {
        try {
            URL url = new URL(source);
            String host = url.getHost();
            return !isIpAddr(host);
        } catch (MalformedURLException ignored) {
            return false;
        }
    }

    public static boolean isIpAddr(String source) {
        if (isEmpty(source)){
            return false;
        }
        return hostPattern.matcher(source).matches();
    }

    /**
     * 多个字符串都不为空
     *
     * @param strings 字符串数组
     */
    public static boolean nonNull(String... strings) {
        for (String obj : strings) {
            if (isEmpty(obj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断IP是否内网IP
     * <p>
     * 私有IP：
     * A类  10.0.0.0-10.255.255.255
     * B类  172.16.0.0-172.31.255.255
     * C类  192.168.0.0-192.168.255.255
     *
     * @param ip 被检测的ip地址
     */
    public static boolean isInnerIp(String ip) {
        boolean isInnerIp = false;
        for (Pattern tmp : innerIpFilter) {
            Matcher matcher = tmp.matcher(ip);
            if (matcher.find()) {
                isInnerIp = true;
                break;
            }
        }
        return isInnerIp;
    }
}
