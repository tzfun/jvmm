package org.beifengtz.jvmm.common.util;

import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 15:24 2021/5/12
 *
 * @author beifengtz
 */
public class IPUtil {
    /**
     * 内网ip过滤列表
     */
    private static final List<Pattern> innerIpFilter = new ArrayList<>();
    private static final Pattern ipPattern = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})((:\\d+)?)(/?)");
    private static final Pattern hostPattern = Pattern.compile("^(((\\d{1,3}\\.){3}\\d{1,3})|((([\\w\\\\-_]+)(\\.[\\w\\\\-_]+))+)):\\d{1,5}$");

    private static String IP = null;

    static {
        Set<String> ipFilter = new HashSet<>();
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

    /**
     * 获取出口IP
     *
     * @return 有可能会获取失败，失败时返回 {@link #getLocalIP()} 的值
     */
    public static String getOutboundIP() {
        if (IP == null) {
            IP = getOutboundIP0();
        }
        return IP;
    }

    /**
     * 获取出口IP
     *
     * @return 有可能会获取失败，失败时返回 {@link #getLocalIP()} 的值
     */
    private static String getOutboundIP0() {
        try {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                return socket.getLocalAddress().getHostAddress();
            }
        } catch (Exception ignored) {
            return getLocalIP();
        }
    }

    /**
     * get IP address, automatically distinguish the operating system.（windows or linux）
     *
     * @return String
     */
    private static String getLocalIP() {
        InetAddress ip = null;
        try {
            if (PlatformUtil.isWindows()) {
                ip = InetAddress.getLocalHost();
            } else {
                if (!InetAddress.getLocalHost().isLoopbackAddress()) {
                    ip = InetAddress.getLocalHost();
                } else {
                    boolean bFindIP = false;
                    Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
                    while (netInterfaces.hasMoreElements()) {
                        if (bFindIP) {
                            break;
                        }
                        NetworkInterface ni = netInterfaces.nextElement();
                        Enumeration<InetAddress> ips = ni.getInetAddresses();
                        while (ips.hasMoreElements()) {
                            ip = ips.nextElement();
                            if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && !ip.getHostAddress().contains(":")) {
                                bFindIP = true;
                                break;
                            }
                        }

                    }
                }
            }
        } catch (Exception ignored) {
        }

        return ip == null ? null : ip.getHostAddress();
    }

    public static boolean isDomainUrl(String source) {
        try {
            URL url = new URL(source);
            String host = url.getHost();
            return !isIpv4(host);
        } catch (MalformedURLException ignored) {
            return false;
        }
    }

    public static boolean isIpv4(String source) {
        if (StringUtil.isEmpty(source)) {
            return false;
        }
        return ipPattern.matcher(source).matches();
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
     * @return true - 如果是ipv4类型的内网地址
     */
    public static boolean isInnerIpv4(String ip) {
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

    public static boolean isHost(String source) {
        if (StringUtil.isEmpty(source)) {
            return false;
        }
        return hostPattern.matcher(source).matches();
    }
}
