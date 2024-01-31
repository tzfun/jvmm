package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;
import oshi.software.os.InternetProtocolStats.TcpState;
import oshi.software.os.InternetProtocolStats.TcpStats;
import oshi.software.os.InternetProtocolStats.UdpStats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description: 网卡信息
 * date 16:08 2023/1/31
 * @author beifengtz
 */
public class NetInfo implements JsonParsable {
    /**
     * 当前连接数
     */
    private long connections;
    private long tcpV4Connections;
    private long tcpV6Connections;
    private long udpV4Connections;
    private long udpV6Connections;
    private final Map<TcpState, Integer> tcpStateConnections = new HashMap<>();
    /**
     * TCP IPV4 连接信息
     */
    private TcpStats tcpV4;
    /**
     * UDP IPV4 连接信息
     */
    private UdpStats udpV4;
    /**
     * TCP IPV6 连接信息
     */
    private TcpStats tcpV6;
    /**
     * TCP IPV6 连接信息
     */
    private UdpStats udpV6;
    /**
     * 各个网卡IO信息
     */
    private final List<NetworkIFInfo> networkIFInfos = new ArrayList<>();

    private NetInfo() {

    }

    public static NetInfo create() {
        return new NetInfo();
    }

    public long getConnections() {
        return connections;
    }

    public NetInfo setConnections(long connections) {
        this.connections = connections;
        return this;
    }

    public long getTcpV4Connections() {
        return tcpV4Connections;
    }

    public NetInfo setTcpV4Connections(long tcpV4Connections) {
        this.tcpV4Connections = tcpV4Connections;
        return this;
    }

    public long getTcpV6Connections() {
        return tcpV6Connections;
    }

    public NetInfo setTcpV6Connections(long tcpV6Connections) {
        this.tcpV6Connections = tcpV6Connections;
        return this;
    }

    public long getUdpV4Connections() {
        return udpV4Connections;
    }

    public NetInfo setUdpV4Connections(long udpV4Connections) {
        this.udpV4Connections = udpV4Connections;
        return this;
    }

    public long getUdpV6Connections() {
        return udpV6Connections;
    }

    public NetInfo setUdpV6Connections(long udpV6Connections) {
        this.udpV6Connections = udpV6Connections;
        return this;
    }

    public Map<TcpState, Integer> getTcpStateConnections() {
        return tcpStateConnections;
    }

    public TcpStats getTcpV4() {
        return tcpV4;
    }

    public NetInfo setTcpV4(TcpStats tcpV4) {
        this.tcpV4 = tcpV4;
        return this;
    }

    public UdpStats getUdpV4() {
        return udpV4;
    }

    public NetInfo setUdpV4(UdpStats udpV4) {
        this.udpV4 = udpV4;
        return this;
    }

    public TcpStats getTcpV6() {
        return tcpV6;
    }

    public NetInfo setTcpV6(TcpStats tcpV6) {
        this.tcpV6 = tcpV6;
        return this;
    }

    public UdpStats getUdpV6() {
        return udpV6;
    }

    public NetInfo setUdpV6(UdpStats udpV6) {
        this.udpV6 = udpV6;
        return this;
    }

    public NetInfo addNetworkIFInfo(NetworkIFInfo info) {
        this.networkIFInfos.add(info);
        return this;
    }

    public List<NetworkIFInfo> getNetworkIFInfos() {
        return networkIFInfos;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }

    public static class NetworkIFInfo implements JsonParsable {

        /**
         * 网卡名
         */
        private String name;
        /**
         * 网卡别名
         */
        private String alias;
        /**
         * mac地址
         */
        private String mac;
        /**
         * IP v4地址
         */
        private String[] ipV4;
        /**
         * IP v6地址
         */
        private String[] ipV6;
        /**
         * 接口运行状态
         */
        private String status;
        /**
         * 接口最大传输单元
         */
        private long mtu;
        /**
         * 当前发送速率
         */
        private double sentBytesPerSecond;
        /**
         * 当前接收速率
         */
        private double recvBytesPerSecond;
        /**
         * 总收包大小，bytes
         */
        private long recvBytes;
        /**
         * 总发包大小，bytes
         */
        private long sentBytes;
        /**
         * 总收包个数
         */
        private long recvCount;
        /**
         * 总发包个数
         */
        private long sentCount;

        private NetworkIFInfo() {

        }

        public static NetworkIFInfo create() {
            return new NetworkIFInfo();
        }

        public String getName() {
            return name;
        }

        public NetworkIFInfo setName(String name) {
            this.name = name;
            return this;
        }

        public String getAlias() {
            return alias;
        }

        public NetworkIFInfo setAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public String getMac() {
            return mac;
        }

        public NetworkIFInfo setMac(String mac) {
            this.mac = mac;
            return this;
        }

        public String[] getIpV4() {
            return ipV4;
        }

        public NetworkIFInfo setIpV4(String[] ipV4) {
            this.ipV4 = ipV4;
            return this;
        }

        public String[] getIpV6() {
            return ipV6;
        }

        public NetworkIFInfo setIpV6(String[] ipV6) {
            this.ipV6 = ipV6;
            return this;
        }

        public String getStatus() {
            return status;
        }

        public NetworkIFInfo setStatus(String status) {
            this.status = status;
            return this;
        }

        public long getMtu() {
            return mtu;
        }

        public NetworkIFInfo setMtu(long mtu) {
            this.mtu = mtu;
            return this;
        }

        public double getSentBytesPerSecond() {
            return sentBytesPerSecond;
        }

        public NetworkIFInfo setSentBytesPerSecond(double sentBytesPerSecond) {
            this.sentBytesPerSecond = sentBytesPerSecond;
            return this;
        }

        public double getRecvBytesPerSecond() {
            return recvBytesPerSecond;
        }

        public NetworkIFInfo setRecvBytesPerSecond(double recvBytesPerSecond) {
            this.recvBytesPerSecond = recvBytesPerSecond;
            return this;
        }

        public long getRecvBytes() {
            return recvBytes;
        }

        public NetworkIFInfo setRecvBytes(long recvBytes) {
            this.recvBytes = recvBytes;
            return this;
        }

        public long getSentBytes() {
            return sentBytes;
        }

        public NetworkIFInfo setSentBytes(long sentBytes) {
            this.sentBytes = sentBytes;
            return this;
        }

        public long getRecvCount() {
            return recvCount;
        }

        public NetworkIFInfo setRecvCount(long recvCount) {
            this.recvCount = recvCount;
            return this;
        }

        public long getSentCount() {
            return sentCount;
        }

        public NetworkIFInfo setSentCount(long sentCount) {
            this.sentCount = sentCount;
            return this;
        }

        @Override
        public String toString() {
            return toJsonStr();
        }
    }
}
