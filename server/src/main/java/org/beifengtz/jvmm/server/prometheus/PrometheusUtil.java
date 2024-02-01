package org.beifengtz.jvmm.server.prometheus;

import org.beifengtz.jvmm.common.exception.MessageSerializeException;
import org.beifengtz.jvmm.core.entity.JvmmData;
import org.beifengtz.jvmm.core.entity.info.*;
import org.beifengtz.jvmm.core.entity.info.NetInfo.NetworkIFInfo;
import org.beifengtz.jvmm.server.prometheus.Types.Label;
import org.beifengtz.jvmm.server.prometheus.Types.Sample;
import org.xerial.snappy.Snappy;
import oshi.software.os.InternetProtocolStats.TcpState;
import oshi.software.os.InternetProtocolStats.TcpStats;
import oshi.software.os.InternetProtocolStats.UdpStats;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * description: TODO
 * date: 18:25 2024/1/29
 *
 * @author beifengtz
 */
public class PrometheusUtil {
    private static final String PROMETHEUS_LABEL_NAME = "__name__";

    public static byte[] pack(JvmmData data) {
        Remote.WriteRequest.Builder writeRequest = Remote.WriteRequest.newBuilder();

        Types.MetricMetadata.Builder metricMetaData = Types.MetricMetadata.newBuilder();
        metricMetaData.setType(Types.MetricMetadata.MetricType.GAUGE);
        metricMetaData.setHelp("helper");
        metricMetaData.setMetricFamilyName("jvmm_gauge");
        writeRequest.addMetadata(metricMetaData.build());

        //  公共标签
        List<Label> labels = new ArrayList<>();
        labels.add(Types.Label.newBuilder().setName("node").setValue(data.getNode()).build());
        long now = System.currentTimeMillis();

        packProcess(data.getProcess(), now, labels, writeRequest);
        packSystem(data.getSys(), now, labels, writeRequest);
        packDiskIO(data.getDiskIO(), now, labels, writeRequest);
        packCpu(data.getCpu(), now, labels, writeRequest);
        packNetwork(data.getNetwork(), now, labels, writeRequest);
        packSysMem(data.getSysMem(), now, labels, writeRequest);
        packSysFile(data.getSysFile(), now, labels, writeRequest);
        packJvmClassLoading(data.getJvmClassLoading(), now, labels, writeRequest);
        packJvmCompilation(data.getJvmCompilation(), now, labels, writeRequest);
        packJvmGc(data.getJvmGc(), now, labels, writeRequest);
        packJvmMem(data.getJvmMemory(), now, labels, writeRequest);
        packJvmMemPool(data.getJvmMemoryPool(), now, labels, writeRequest);
        packJvmThread(data.getJvmThread(), now, labels, writeRequest);
        try {
            return Snappy.compress(writeRequest.build().toByteArray());
        } catch (IOException e) {
            throw new MessageSerializeException(e);
        }
    }

    /**
     * 组装进程信息到Prometheus结构
     *
     * @param process      进程信息
     * @param timestamp    统计时间戳
     * @param labels       通用标签
     * @param writeRequest Request
     */
    private static void packProcess(ProcessInfo process, long timestamp, List<Types.Label> labels,
                                    Remote.WriteRequest.Builder writeRequest) {
        if (process == null) {
            return;
        }
        Types.TimeSeries.Builder processTimeSeries = Types.TimeSeries.newBuilder();
        processTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("p_up_time").build());
        processTimeSeries.addLabels(Types.Label.newBuilder().setName("vm_name").setValue(process.getVmName()).build());
        processTimeSeries.addLabels(Types.Label.newBuilder().setName("vm_version").setValue(process.getVmVersion()).build());
        processTimeSeries.addAllLabels(labels);
        processTimeSeries.addSamples(Types.Sample.newBuilder().setTimestamp(timestamp).setValue(process.getUptime()).build());
        writeRequest.addTimeseries(processTimeSeries.build());
    }

    /**
     * 组装操作系统信息到Prometheus结构
     *
     * @param sys          操作系统信息
     * @param timestamp    统计时间戳
     * @param labels       通用标签
     * @param writeRequest Request
     */
    private static void packSystem(SysInfo sys, long timestamp, List<Types.Label> labels,
                                   Remote.WriteRequest.Builder writeRequest) {
        if (sys == null) {
            return;
        }
        double loadAverage = sys.getLoadAverage();
        Types.TimeSeries.Builder osTimeSeries = Types.TimeSeries.newBuilder();
        osTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_load_avg").build());
        osTimeSeries.addLabels(Types.Label.newBuilder().setName("os_name").setValue(sys.getName()).build());
        osTimeSeries.addLabels(Types.Label.newBuilder().setName("os_version").setValue(sys.getVersion()).build());
        osTimeSeries.addLabels(Types.Label.newBuilder().setName("os_arch").setValue(sys.getArch()).build());
        osTimeSeries.addAllLabels(labels);
        osTimeSeries.addSamples(Types.Sample.newBuilder().setTimestamp(timestamp).setValue(Math.max(loadAverage, 0)).build());
        writeRequest.addTimeseries(osTimeSeries.build());

        Types.TimeSeries.Builder osCPUNumTimeSeries = Types.TimeSeries.newBuilder();
        osCPUNumTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_cpu").build());
        osCPUNumTimeSeries.addLabels(Types.Label.newBuilder().setName("os_name").setValue(sys.getName()).build());
        osCPUNumTimeSeries.addLabels(Types.Label.newBuilder().setName("os_version").setValue(sys.getVersion()).build());
        osCPUNumTimeSeries.addLabels(Types.Label.newBuilder().setName("os_arch").setValue(sys.getArch()).build());
        osCPUNumTimeSeries.addAllLabels(labels);
        osCPUNumTimeSeries.addSamples(Types.Sample.newBuilder().setTimestamp(timestamp).setValue(Math.max(sys.getCpuNum(), 0)).build());
        writeRequest.addTimeseries(osCPUNumTimeSeries.build());
    }

    /**
     * 组装磁盘IO数据到Prometheus结构
     *
     * @param disks        磁盘IO信息
     * @param timestamp    统计时间戳
     * @param labels       通用标签
     * @param writeRequest Request
     */
    private static void packDiskIO(List<DiskIOInfo> disks, long timestamp, List<Types.Label> labels,
                                   Remote.WriteRequest.Builder writeRequest) {
        if (disks == null) {
            return;
        }
        for (DiskIOInfo disk : disks) {
            Label nameLabel = Label.newBuilder().setName("disk_name").setValue(disk.getName()).build();

            //  磁盘读次数速度
            Types.TimeSeries.Builder diskReadSpeedTimeSeries = Types.TimeSeries.newBuilder();
            diskReadSpeedTimeSeries.addLabels(nameLabel);
            diskReadSpeedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("disk_read_speed").build());
            diskReadSpeedTimeSeries.addAllLabels(labels);
            diskReadSpeedTimeSeries.addSamples(Types.Sample.newBuilder()
                    .setTimestamp(timestamp)
                    .setValue(disk.getReadPerSecond())
                    .build());
            writeRequest.addTimeseries(diskReadSpeedTimeSeries.build());

            //  磁盘读bytes速度
            Types.TimeSeries.Builder diskReadBytesSpeedTimeSeries = Types.TimeSeries.newBuilder();
            diskReadBytesSpeedTimeSeries.addLabels(nameLabel);
            diskReadBytesSpeedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("disk_read_bytes_speed").build());
            diskReadBytesSpeedTimeSeries.addAllLabels(labels);
            diskReadBytesSpeedTimeSeries.addSamples(Types.Sample.newBuilder()
                    .setTimestamp(timestamp)
                    .setValue(disk.getReadBytesPerSecond())
                    .build());
            writeRequest.addTimeseries(diskReadBytesSpeedTimeSeries.build());

            //  磁盘写次数速度
            Types.TimeSeries.Builder diskWriteSpeedTimeSeries = Types.TimeSeries.newBuilder();
            diskWriteSpeedTimeSeries.addLabels(nameLabel);
            diskWriteSpeedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("disk_write_speed").build());
            diskWriteSpeedTimeSeries.addAllLabels(labels);
            diskWriteSpeedTimeSeries.addSamples(Types.Sample.newBuilder()
                    .setTimestamp(timestamp)
                    .setValue(disk.getWritePerSecond())
                    .build());
            writeRequest.addTimeseries(diskWriteSpeedTimeSeries.build());

            //  磁盘写bytes速度
            Types.TimeSeries.Builder diskWriteBytesSpeedTimeSeries = Types.TimeSeries.newBuilder();
            diskWriteBytesSpeedTimeSeries.addLabels(nameLabel);
            diskWriteBytesSpeedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("disk_write_bytes_speed").build());
            diskWriteBytesSpeedTimeSeries.addAllLabels(labels);
            diskWriteBytesSpeedTimeSeries.addSamples(Types.Sample.newBuilder()
                    .setTimestamp(timestamp)
                    .setValue(disk.getReadBytesPerSecond())
                    .build());
            writeRequest.addTimeseries(diskWriteBytesSpeedTimeSeries.build());


            Types.TimeSeries.Builder diskQueueLenTimeSeries = Types.TimeSeries.newBuilder();
            diskQueueLenTimeSeries.addLabels(nameLabel);
            diskQueueLenTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("disk_queue_len").build());
            diskQueueLenTimeSeries.addAllLabels(labels);
            diskQueueLenTimeSeries.addSamples(Types.Sample.newBuilder()
                    .setTimestamp(timestamp)
                    .setValue(disk.getCurrentQueueLength())
                    .build());
            writeRequest.addTimeseries(diskQueueLenTimeSeries.build());
        }
    }

    /**
     * 组装CPU数据到Prometheus结构
     *
     * @param cpu          CPU信息
     * @param timestamp    统计时间戳
     * @param labels       通用标签
     * @param writeRequest Request
     */
    private static void packCpu(CPUInfo cpu, long timestamp, List<Types.Label> labels,
                                Remote.WriteRequest.Builder writeRequest) {
        if (cpu == null) {
            return;
        }
        Types.Label cpuNumLabel = Types.Label.newBuilder().setName("cpu_num").setValue(String.valueOf(cpu.getCpuNum())).build();

        Types.TimeSeries.Builder cpuSysUsageTimeSeries = Types.TimeSeries.newBuilder();
        cpuSysUsageTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("cpu_sys_usage").build());
        cpuSysUsageTimeSeries.addLabels(cpuNumLabel);
        cpuSysUsageTimeSeries.addAllLabels(labels);
        cpuSysUsageTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(cpu.getSys()).build());
        writeRequest.addTimeseries(cpuSysUsageTimeSeries);

        Types.TimeSeries.Builder cpuUserUsageTimeSeries = Types.TimeSeries.newBuilder();
        cpuUserUsageTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("cpu_user_usage").build());
        cpuUserUsageTimeSeries.addLabels(cpuNumLabel);
        cpuUserUsageTimeSeries.addAllLabels(labels);
        cpuUserUsageTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(cpu.getUser()).build());
        writeRequest.addTimeseries(cpuUserUsageTimeSeries);

        Types.TimeSeries.Builder cpuIOWaitTimeSeries = Types.TimeSeries.newBuilder();
        cpuIOWaitTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("cpu_io_wait").build());
        cpuIOWaitTimeSeries.addLabels(cpuNumLabel);
        cpuIOWaitTimeSeries.addAllLabels(labels);
        cpuIOWaitTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(cpu.getIoWait()).build());
        writeRequest.addTimeseries(cpuIOWaitTimeSeries);

        Types.TimeSeries.Builder cpuIdleTimeSeries = Types.TimeSeries.newBuilder();
        cpuIdleTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("cpu_idle").build());
        cpuIdleTimeSeries.addLabels(cpuNumLabel);
        cpuIdleTimeSeries.addAllLabels(labels);
        cpuIdleTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(cpu.getIdle()).build());
        writeRequest.addTimeseries(cpuIdleTimeSeries);
    }

    /**
     * 组装网卡数据到Prometheus结构
     *
     * @param network      网卡信息
     * @param timestamp    统计时间戳
     * @param labels       通用标签
     * @param writeRequest Request
     */
    private static void packNetwork(NetInfo network, long timestamp, List<Types.Label> labels,
                                    Remote.WriteRequest.Builder writeRequest) {
        if (network == null) {
            return;
        }

        Types.TimeSeries.Builder netConnectionsTimeSeries = Types.TimeSeries.newBuilder();
        netConnectionsTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_conn").build());
        netConnectionsTimeSeries.addAllLabels(labels);
        netConnectionsTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(network.getConnections()).build());
        writeRequest.addTimeseries(netConnectionsTimeSeries);

        Types.TimeSeries.Builder netTcpV4ConnectionsTimeSeries = Types.TimeSeries.newBuilder();
        netTcpV4ConnectionsTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_tcp4_conn").build());
        netTcpV4ConnectionsTimeSeries.addAllLabels(labels);
        netTcpV4ConnectionsTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(network.getTcpV4Connections()).build());
        writeRequest.addTimeseries(netTcpV4ConnectionsTimeSeries);

        Types.TimeSeries.Builder netTcpV6ConnectionsTimeSeries = Types.TimeSeries.newBuilder();
        netTcpV6ConnectionsTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_tcp6_conn").build());
        netTcpV6ConnectionsTimeSeries.addAllLabels(labels);
        netTcpV6ConnectionsTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(network.getTcpV6Connections()).build());
        writeRequest.addTimeseries(netTcpV6ConnectionsTimeSeries);

        Types.TimeSeries.Builder netUdpV4ConnectionsTimeSeries = Types.TimeSeries.newBuilder();
        netUdpV4ConnectionsTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_udp4_conn").build());
        netUdpV4ConnectionsTimeSeries.addAllLabels(labels);
        netUdpV4ConnectionsTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(network.getUdpV4Connections()).build());
        writeRequest.addTimeseries(netUdpV4ConnectionsTimeSeries);

        Types.TimeSeries.Builder netUdpV6ConnectionsTimeSeries = Types.TimeSeries.newBuilder();
        netUdpV6ConnectionsTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_udp6_conn").build());
        netUdpV6ConnectionsTimeSeries.addAllLabels(labels);
        netUdpV6ConnectionsTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(network.getUdpV6Connections()).build());
        writeRequest.addTimeseries(netUdpV6ConnectionsTimeSeries);

        for (Entry<TcpState, Integer> entry : network.getTcpStateConnections().entrySet()) {
            Types.TimeSeries.Builder netStateTimeSeries = Types.TimeSeries.newBuilder();
            netStateTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_conn_" + entry.getKey()).build());
            netStateTimeSeries.addAllLabels(labels);
            netStateTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(entry.getValue()).build());
            writeRequest.addTimeseries(netStateTimeSeries);
        }

        buildTcpStats(network.getTcpV4(), "net_tcp4", timestamp, labels, writeRequest);
        buildTcpStats(network.getTcpV6(), "net_tcp6", timestamp, labels, writeRequest);
        buildUdpStats(network.getUdpV4(), "net_udp4", timestamp, labels, writeRequest);
        buildUdpStats(network.getUdpV6(), "net_udp6", timestamp, labels, writeRequest);

        List<NetworkIFInfo> networkIFInfos = network.getNetworkIFInfos();
        if (networkIFInfos != null) {
            for (NetworkIFInfo info : networkIFInfos) {
                Types.Label ifName = Types.Label.newBuilder().setName("net_if_name").setValue(info.getName()).build();
                Types.Label ifAlias = Types.Label.newBuilder().setName("net_if_alias").setValue(info.getAlias()).build();

                Types.TimeSeries.Builder ifSentSpeedTimeSeries = Types.TimeSeries.newBuilder();
                ifSentSpeedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_if_sent_speed").build());
                ifSentSpeedTimeSeries.addLabels(ifName);
                ifSentSpeedTimeSeries.addLabels(ifAlias);
                ifSentSpeedTimeSeries.addAllLabels(labels);
                ifSentSpeedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(info.getSentBytesPerSecond()).build());
                writeRequest.addTimeseries(ifSentSpeedTimeSeries);

                Types.TimeSeries.Builder ifRecvSpeedTimeSeries = Types.TimeSeries.newBuilder();
                ifRecvSpeedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_if_recv_speed").build());
                ifRecvSpeedTimeSeries.addLabels(ifName);
                ifRecvSpeedTimeSeries.addLabels(ifAlias);
                ifRecvSpeedTimeSeries.addAllLabels(labels);
                ifRecvSpeedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(info.getRecvBytesPerSecond()).build());
                writeRequest.addTimeseries(ifRecvSpeedTimeSeries);

                Types.TimeSeries.Builder ifSentBytesTimeSeries = Types.TimeSeries.newBuilder();
                ifSentBytesTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_if_sent_bytes").build());
                ifSentBytesTimeSeries.addLabels(ifName);
                ifSentBytesTimeSeries.addLabels(ifAlias);
                ifSentBytesTimeSeries.addAllLabels(labels);
                ifSentBytesTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(info.getSentBytes()).build());
                writeRequest.addTimeseries(ifSentBytesTimeSeries);

                Types.TimeSeries.Builder ifRecvBytesTimeSeries = Types.TimeSeries.newBuilder();
                ifRecvBytesTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_if_recv_bytes").build());
                ifRecvBytesTimeSeries.addLabels(ifName);
                ifRecvBytesTimeSeries.addLabels(ifAlias);
                ifRecvBytesTimeSeries.addAllLabels(labels);
                ifRecvBytesTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(info.getRecvBytes()).build());
                writeRequest.addTimeseries(ifRecvBytesTimeSeries);

                Types.TimeSeries.Builder ifSentCountTimeSeries = Types.TimeSeries.newBuilder();
                ifSentCountTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_if_sent_count").build());
                ifSentCountTimeSeries.addLabels(ifName);
                ifSentCountTimeSeries.addLabels(ifAlias);
                ifSentCountTimeSeries.addAllLabels(labels);
                ifSentCountTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(info.getSentCount()).build());
                writeRequest.addTimeseries(ifSentCountTimeSeries);

                Types.TimeSeries.Builder ifRecvCountTimeSeries = Types.TimeSeries.newBuilder();
                ifRecvCountTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_if_recv_count").build());
                ifRecvCountTimeSeries.addLabels(ifName);
                ifRecvCountTimeSeries.addLabels(ifAlias);
                ifRecvCountTimeSeries.addAllLabels(labels);
                ifRecvCountTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(info.getSentCount()).build());
                writeRequest.addTimeseries(ifRecvCountTimeSeries);
            }
        }
    }

    private static void buildTcpStats(TcpStats stats, String namePrefix, long timestamp, List<Types.Label> labels,
                                      Remote.WriteRequest.Builder writeRequest) {
        if (stats == null) {
            return;
        }
        Types.TimeSeries.Builder tcpConnectionsEstablishedTimeSeries = Types.TimeSeries.newBuilder();
        tcpConnectionsEstablishedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue(namePrefix + "_conn_established").build());
        tcpConnectionsEstablishedTimeSeries.addAllLabels(labels);
        tcpConnectionsEstablishedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(stats.getConnectionsEstablished()).build());
        writeRequest.addTimeseries(tcpConnectionsEstablishedTimeSeries);

        Types.TimeSeries.Builder tcpConnectionsActiveTimeSeries = Types.TimeSeries.newBuilder();
        tcpConnectionsActiveTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue(namePrefix + "_conn_active").build());
        tcpConnectionsActiveTimeSeries.addAllLabels(labels);
        tcpConnectionsActiveTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(stats.getConnectionsActive()).build());
        writeRequest.addTimeseries(tcpConnectionsActiveTimeSeries);

        Types.TimeSeries.Builder tcpConnectionsPassiveTimeSeries = Types.TimeSeries.newBuilder();
        tcpConnectionsPassiveTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue(namePrefix + "_conn_passive").build());
        tcpConnectionsPassiveTimeSeries.addAllLabels(labels);
        tcpConnectionsPassiveTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(stats.getConnectionsPassive()).build());
        writeRequest.addTimeseries(tcpConnectionsPassiveTimeSeries);

        Types.TimeSeries.Builder tcpConnectionFailTimeSeries = Types.TimeSeries.newBuilder();
        tcpConnectionFailTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue(namePrefix + "_conn_fail").build());
        tcpConnectionFailTimeSeries.addAllLabels(labels);
        tcpConnectionFailTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(stats.getConnectionFailures()).build());
        writeRequest.addTimeseries(tcpConnectionFailTimeSeries);

        Types.TimeSeries.Builder tcpConnectionResetTimeSeries = Types.TimeSeries.newBuilder();
        tcpConnectionResetTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue(namePrefix + "_conn_reset").build());
        tcpConnectionResetTimeSeries.addAllLabels(labels);
        tcpConnectionResetTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(stats.getConnectionsReset()).build());
        writeRequest.addTimeseries(tcpConnectionResetTimeSeries);

        Types.TimeSeries.Builder tcpSegSentTimeSeries = Types.TimeSeries.newBuilder();
        tcpSegSentTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue(namePrefix + "_seg_sent").build());
        tcpSegSentTimeSeries.addAllLabels(labels);
        tcpSegSentTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(stats.getSegmentsSent()).build());
        writeRequest.addTimeseries(tcpSegSentTimeSeries);

        Types.TimeSeries.Builder tcpSegReceivedTimeSeries = Types.TimeSeries.newBuilder();
        tcpSegReceivedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue(namePrefix + "_seg_received").build());
        tcpSegReceivedTimeSeries.addAllLabels(labels);
        tcpSegReceivedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(stats.getSegmentsReceived()).build());
        writeRequest.addTimeseries(tcpSegReceivedTimeSeries);

        Types.TimeSeries.Builder tcpInErrTimeSeries = Types.TimeSeries.newBuilder();
        tcpInErrTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue(namePrefix + "_in_err").build());
        tcpInErrTimeSeries.addAllLabels(labels);
        tcpInErrTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(stats.getInErrors()).build());
        writeRequest.addTimeseries(tcpInErrTimeSeries);

        Types.TimeSeries.Builder tcpOutResetTimeSeries = Types.TimeSeries.newBuilder();
        tcpOutResetTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue(namePrefix + "_out_reset").build());
        tcpOutResetTimeSeries.addAllLabels(labels);
        tcpOutResetTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(stats.getOutResets()).build());
        writeRequest.addTimeseries(tcpOutResetTimeSeries);
    }

    private static void buildUdpStats(UdpStats stats, String namePrefix, long timestamp, List<Types.Label> labels,
                                      Remote.WriteRequest.Builder writeRequest) {
        if (stats == null) {
            return;
        }

        Types.TimeSeries.Builder tcpSegSentTimeSeries = Types.TimeSeries.newBuilder();
        tcpSegSentTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue(namePrefix + "_sent").build());
        tcpSegSentTimeSeries.addAllLabels(labels);
        tcpSegSentTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(stats.getDatagramsSent()).build());
        writeRequest.addTimeseries(tcpSegSentTimeSeries);

        Types.TimeSeries.Builder tcpSegReceivedTimeSeries = Types.TimeSeries.newBuilder();
        tcpSegReceivedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue(namePrefix + "_received").build());
        tcpSegReceivedTimeSeries.addAllLabels(labels);
        tcpSegReceivedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(stats.getDatagramsReceived()).build());
        writeRequest.addTimeseries(tcpSegReceivedTimeSeries);

    }

    /**
     * 组装系统内存数据到Prometheus结构
     *
     * @param sysMem       系统内存信息
     * @param timestamp    统计时间戳
     * @param labels       通用标签
     * @param writeRequest Request
     */
    private static void packSysMem(SysMemInfo sysMem, long timestamp, List<Types.Label> labels,
                                   Remote.WriteRequest.Builder writeRequest) {
        if (sysMem == null) {
            return;
        }

        Types.TimeSeries.Builder memFreePresentTimeSeries = Types.TimeSeries.newBuilder();
        memFreePresentTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_mem_usage").build());
        memFreePresentTimeSeries.addAllLabels(labels);
        memFreePresentTimeSeries.addSamples(Sample.newBuilder()
                .setTimestamp(timestamp)
                .setValue(sysMem.getTotalPhysical() == 0 ? 0 : (100.0 * (sysMem.getTotalPhysical() - sysMem.getFreePhysical()) / sysMem.getTotalPhysical()))
                .build());
        writeRequest.addTimeseries(memFreePresentTimeSeries);

        Types.TimeSeries.Builder memCommittedTimeSeries = Types.TimeSeries.newBuilder();
        memCommittedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_mem_committed").build());
        memCommittedTimeSeries.addAllLabels(labels);
        memCommittedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(sysMem.getCommittedVirtual()).build());
        writeRequest.addTimeseries(memCommittedTimeSeries);

        Types.TimeSeries.Builder memTotalTimeSeries = Types.TimeSeries.newBuilder();
        memTotalTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_mem_total").build());
        memTotalTimeSeries.addAllLabels(labels);
        memTotalTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(sysMem.getTotalPhysical()).build());
        writeRequest.addTimeseries(memTotalTimeSeries);

        Types.TimeSeries.Builder memFreeTimeSeries = Types.TimeSeries.newBuilder();
        memFreeTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_mem_free").build());
        memFreeTimeSeries.addAllLabels(labels);
        memFreeTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(sysMem.getFreePhysical()).build());
        writeRequest.addTimeseries(memFreeTimeSeries);

        Types.TimeSeries.Builder memTotalSwapTimeSeries = Types.TimeSeries.newBuilder();
        memTotalSwapTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_mem_total_swap").build());
        memTotalSwapTimeSeries.addAllLabels(labels);
        memTotalSwapTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(sysMem.getTotalSwap()).build());
        writeRequest.addTimeseries(memTotalSwapTimeSeries);

        Types.TimeSeries.Builder memFreeSwapTimeSeries = Types.TimeSeries.newBuilder();
        memFreeSwapTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_mem_free_swap").build());
        memFreeSwapTimeSeries.addAllLabels(labels);
        memFreeSwapTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(sysMem.getFreeSwap()).build());
        writeRequest.addTimeseries(memFreeSwapTimeSeries);

        Types.TimeSeries.Builder memBufferCacheTimeSeries = Types.TimeSeries.newBuilder();
        memBufferCacheTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_mem_buffer_cache").build());
        memBufferCacheTimeSeries.addAllLabels(labels);
        memBufferCacheTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(sysMem.getBufferCache()).build());
        writeRequest.addTimeseries(memBufferCacheTimeSeries);

        Types.TimeSeries.Builder memSharedTimeSeries = Types.TimeSeries.newBuilder();
        memSharedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_mem_shared").build());
        memSharedTimeSeries.addAllLabels(labels);
        memSharedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(sysMem.getShared()).build());
        writeRequest.addTimeseries(memSharedTimeSeries);
    }

    /**
     * 组装系统盘数据到Prometheus结构
     *
     * @param sysFileList  系统盘信息
     * @param timestamp    统计时间戳
     * @param labels       通用标签
     * @param writeRequest Request
     */
    private static void packSysFile(List<SysFileInfo> sysFileList, long timestamp, List<Types.Label> labels,
                                    Remote.WriteRequest.Builder writeRequest) {
        if (sysFileList == null) {
            return;
        }
        long total = 0;
        long usable = 0;
        for (SysFileInfo sysFile : sysFileList) {
            Types.Label nameLabel = Types.Label.newBuilder().setName("file_name").setValue(sysFile.getName()).build();
            Types.Label labelLabel = Types.Label.newBuilder().setName("file_label").setValue(sysFile.getLabel()).build();
            Types.Label typeLabel = Types.Label.newBuilder().setName("file_type").setValue(sysFile.getType()).build();
            Types.Label mountLabel = Types.Label.newBuilder().setName("file_mount").setValue(sysFile.getMount()).build();

            Types.TimeSeries.Builder osFileSizeTimeSeries = Types.TimeSeries.newBuilder();
            osFileSizeTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_file_total").build());
            osFileSizeTimeSeries.addLabels(nameLabel);
            osFileSizeTimeSeries.addLabels(labelLabel);
            osFileSizeTimeSeries.addLabels(typeLabel);
            osFileSizeTimeSeries.addLabels(mountLabel);
            osFileSizeTimeSeries.addAllLabels(labels);
            osFileSizeTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(sysFile.getSize()).build());
            writeRequest.addTimeseries(osFileSizeTimeSeries);

            Types.TimeSeries.Builder osFileFreeTimeSeries = Types.TimeSeries.newBuilder();
            osFileFreeTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_file_free").build());
            osFileFreeTimeSeries.addLabels(nameLabel);
            osFileFreeTimeSeries.addLabels(labelLabel);
            osFileFreeTimeSeries.addLabels(typeLabel);
            osFileFreeTimeSeries.addLabels(mountLabel);
            osFileFreeTimeSeries.addAllLabels(labels);
            osFileFreeTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(sysFile.getFree()).build());
            writeRequest.addTimeseries(osFileFreeTimeSeries);

            Types.TimeSeries.Builder osFileUsableTimeSeries = Types.TimeSeries.newBuilder();
            osFileUsableTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_file_usable").build());
            osFileUsableTimeSeries.addLabels(nameLabel);
            osFileUsableTimeSeries.addLabels(labelLabel);
            osFileUsableTimeSeries.addLabels(typeLabel);
            osFileUsableTimeSeries.addLabels(mountLabel);
            osFileUsableTimeSeries.addAllLabels(labels);
            osFileUsableTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(sysFile.getUsable()).build());
            writeRequest.addTimeseries(osFileUsableTimeSeries);

            Types.TimeSeries.Builder osFileUsablePresentTimeSeries = Types.TimeSeries.newBuilder();
            osFileUsablePresentTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_file_usage").build());
            osFileUsablePresentTimeSeries.addLabels(nameLabel);
            osFileUsablePresentTimeSeries.addLabels(labelLabel);
            osFileUsablePresentTimeSeries.addLabels(typeLabel);
            osFileUsablePresentTimeSeries.addLabels(mountLabel);
            osFileUsablePresentTimeSeries.addAllLabels(labels);
            osFileUsablePresentTimeSeries.addSamples(Sample.newBuilder()
                    .setTimestamp(timestamp)
                    .setValue(sysFile.getSize() == 0 ? 0 : (100.0 * sysFile.getUsable() / sysFile.getSize()))
                    .build());
            writeRequest.addTimeseries(osFileUsablePresentTimeSeries);
            total += sysFile.getSize();
            usable += sysFile.getUsable();
        }

        Types.TimeSeries.Builder osFileTotalUsablePresentTimeSeries = Types.TimeSeries.newBuilder();
        osFileTotalUsablePresentTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("os_file_usage_total").build());
        osFileTotalUsablePresentTimeSeries.addAllLabels(labels);
        osFileTotalUsablePresentTimeSeries.addSamples(Sample.newBuilder()
                .setTimestamp(timestamp)
                .setValue(total == 0 ? 0 : (100.0 * (total-usable) / total))
                .build());
        writeRequest.addTimeseries(osFileTotalUsablePresentTimeSeries);
    }

    /**
     * 组装JVM 类加载数据到Prometheus结构
     *
     * @param classLoading 类加载数据
     * @param timestamp    统计时间戳
     * @param labels       通用标签
     * @param writeRequest Request
     */
    private static void packJvmClassLoading(JvmClassLoadingInfo classLoading, long timestamp, List<Types.Label> labels,
                                            Remote.WriteRequest.Builder writeRequest) {
        if (classLoading == null) {
            return;
        }
        Types.TimeSeries.Builder jvmLoadedClassTimeSeries = Types.TimeSeries.newBuilder();
        jvmLoadedClassTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_loaded_class").build());
        jvmLoadedClassTimeSeries.addAllLabels(labels);
        jvmLoadedClassTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(classLoading.getLoadedClassCount()).build());
        writeRequest.addTimeseries(jvmLoadedClassTimeSeries);

        Types.TimeSeries.Builder jvmUnloadedClassTimeSeries = Types.TimeSeries.newBuilder();
        jvmUnloadedClassTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_unloaded_class").build());
        jvmUnloadedClassTimeSeries.addAllLabels(labels);
        jvmUnloadedClassTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(classLoading.getUnLoadedClassCount()).build());
        writeRequest.addTimeseries(jvmUnloadedClassTimeSeries);

        Types.TimeSeries.Builder jvmTotalLoadedClassTimeSeries = Types.TimeSeries.newBuilder();
        jvmTotalLoadedClassTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_loaded_class_total").build());
        jvmTotalLoadedClassTimeSeries.addAllLabels(labels);
        jvmTotalLoadedClassTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(classLoading.getTotalLoadedClassCount()).build());
        writeRequest.addTimeseries(jvmTotalLoadedClassTimeSeries);
    }

    /**
     * 组装JVM编译数据到Prometheus结构
     *
     * @param compilation  编译数据
     * @param timestamp    统计时间戳
     * @param labels       通用标签
     * @param writeRequest Request
     */
    private static void packJvmCompilation(JvmCompilationInfo compilation, long timestamp, List<Types.Label> labels,
                                           Remote.WriteRequest.Builder writeRequest) {
        if (compilation == null) {
            return;
        }
        Types.TimeSeries.Builder jvmCompilationTimeSeries = Types.TimeSeries.newBuilder();
        jvmCompilationTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_compilation_time").build());
        jvmCompilationTimeSeries.addLabels(Types.Label.newBuilder().setName("jvm_compilation_name").setValue(compilation.getName()).build());
        jvmCompilationTimeSeries.addAllLabels(labels);
        jvmCompilationTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(compilation.getTotalCompilationTime()).build());
        writeRequest.addTimeseries(jvmCompilationTimeSeries);
    }

    /**
     * 组装JVM垃圾回收器数据到Prometheus结构
     *
     * @param gcList       垃圾回收器数据
     * @param timestamp    统计时间戳
     * @param labels       通用标签
     * @param writeRequest Request
     */
    private static void packJvmGc(List<JvmGCInfo> gcList, long timestamp, List<Types.Label> labels,
                                  Remote.WriteRequest.Builder writeRequest) {
        if (gcList == null) {
            return;
        }
        for (JvmGCInfo gc : gcList) {
            Types.Label nameLabel = Types.Label.newBuilder().setName("jvm_gc_name").setValue(gc.getName()).build();

            Types.TimeSeries.Builder jvmGcTimeTimeSeries = Types.TimeSeries.newBuilder();
            jvmGcTimeTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_gc_time").build());
            jvmGcTimeTimeSeries.addLabels(nameLabel);
            jvmGcTimeTimeSeries.addAllLabels(labels);
            jvmGcTimeTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(gc.getCollectionTime()).build());
            writeRequest.addTimeseries(jvmGcTimeTimeSeries);

            Types.TimeSeries.Builder jvmGcCountTimeSeries = Types.TimeSeries.newBuilder();
            jvmGcCountTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_gc_count").build());
            jvmGcCountTimeSeries.addLabels(nameLabel);
            jvmGcCountTimeSeries.addAllLabels(labels);
            jvmGcCountTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(gc.getCollectionCount()).build());
            writeRequest.addTimeseries(jvmGcCountTimeSeries);
        }
    }

    /**
     * 组装JVM内存数据到Prometheus结构
     *
     * @param mem          JVM内存信息
     * @param timestamp    统计时间戳
     * @param labels       通用标签
     * @param writeRequest Request
     */
    private static void packJvmMem(JvmMemoryInfo mem, long timestamp, List<Types.Label> labels,
                                   Remote.WriteRequest.Builder writeRequest) {

        Types.TimeSeries.Builder jvmMemPendingCountTimeSeries = Types.TimeSeries.newBuilder();
        jvmMemPendingCountTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_mem_pending").build());
        jvmMemPendingCountTimeSeries.addAllLabels(labels);
        jvmMemPendingCountTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(mem.getPendingCount()).build());
        writeRequest.addTimeseries(jvmMemPendingCountTimeSeries);

        MemoryUsageInfo heapUsage = mem.getHeapUsage();
        if (heapUsage != null) {

            Types.Label initLabel = Types.Label.newBuilder().setName("jvm_mem_heap_init").setValue(String.valueOf(heapUsage.getInit())).build();
            Types.Label maxLabel = Types.Label.newBuilder().setName("jvm_mem_heap_max").setValue(String.valueOf(heapUsage.getMax())).build();
            Types.TimeSeries.Builder jvmMemHeapUsedTimeSeries = Types.TimeSeries.newBuilder();
            jvmMemHeapUsedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_mem_heap_used").build());
            jvmMemHeapUsedTimeSeries.addLabels(initLabel);
            jvmMemHeapUsedTimeSeries.addLabels(maxLabel);
            jvmMemHeapUsedTimeSeries.addAllLabels(labels);
            jvmMemHeapUsedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(heapUsage.getUsed()).build());
            writeRequest.addTimeseries(jvmMemHeapUsedTimeSeries);

            Types.TimeSeries.Builder jvmMemHeapCommittedTimeSeries = Types.TimeSeries.newBuilder();
            jvmMemHeapCommittedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_mem_heap_committed").build());
            jvmMemHeapCommittedTimeSeries.addLabels(initLabel);
            jvmMemHeapCommittedTimeSeries.addLabels(maxLabel);
            jvmMemHeapCommittedTimeSeries.addAllLabels(labels);
            jvmMemHeapCommittedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(heapUsage.getCommitted()).build());
            writeRequest.addTimeseries(jvmMemHeapCommittedTimeSeries);
        }

        MemoryUsageInfo nonHeapUsage = mem.getNonHeapUsage();
        if (nonHeapUsage != null) {
            Types.Label initLabel = Types.Label.newBuilder().setName("jvm_mem_nonheap_init").setValue(String.valueOf(nonHeapUsage.getInit())).build();
            Types.Label maxLabel = Types.Label.newBuilder().setName("jvm_mem_nonheap_max").setValue(String.valueOf(nonHeapUsage.getMax())).build();
            Types.TimeSeries.Builder jvmMemHeapUsedTimeSeries = Types.TimeSeries.newBuilder();
            jvmMemHeapUsedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_mem_nonheap_used").build());
            jvmMemHeapUsedTimeSeries.addLabels(initLabel);
            jvmMemHeapUsedTimeSeries.addLabels(maxLabel);
            jvmMemHeapUsedTimeSeries.addAllLabels(labels);
            jvmMemHeapUsedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(nonHeapUsage.getUsed()).build());
            writeRequest.addTimeseries(jvmMemHeapUsedTimeSeries);

            Types.TimeSeries.Builder jvmMemHeapCommittedTimeSeries = Types.TimeSeries.newBuilder();
            jvmMemHeapCommittedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_mem_nonheap_committed").build());
            jvmMemHeapCommittedTimeSeries.addLabels(initLabel);
            jvmMemHeapCommittedTimeSeries.addLabels(maxLabel);
            jvmMemHeapCommittedTimeSeries.addAllLabels(labels);
            jvmMemHeapCommittedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(nonHeapUsage.getCommitted()).build());
            writeRequest.addTimeseries(jvmMemHeapCommittedTimeSeries);
        }
    }

    /**
     * 组装JVM内存池数据到Prometheus结构
     *
     * @param poolList     JVM内存池信息
     * @param timestamp    统计时间戳
     * @param labels       通用标签
     * @param writeRequest Request
     */
    private static void packJvmMemPool(List<JvmMemoryPoolInfo> poolList, long timestamp, List<Types.Label> labels,
                                       Remote.WriteRequest.Builder writeRequest) {
        if (poolList == null) {
            return;
        }
        for (JvmMemoryPoolInfo pool : poolList) {
            Types.Label nameLabel = Types.Label.newBuilder().setName("jvm_mem_pool_name").setValue(pool.getName()).build();
            Types.Label typeLabel = Types.Label.newBuilder().setName("jvm_mem_pool_type").setValue(pool.getType().name()).build();
            Types.Label gcThresholdLabel = Types.Label.newBuilder().setName("jvm_mem_pool_gc_threshold").setValue(String.valueOf(pool.getCollectionUsageThreshold())).build();
            Types.Label thresholdLabel = Types.Label.newBuilder().setName("jvm_mem_pool_threshold").setValue(String.valueOf(pool.getUsageThreshold())).build();

            MemoryUsageInfo usage = pool.getPeakUsage();
            if (usage != null) {
                Types.Label initLabel = Types.Label.newBuilder().setName("jvm_mem_pool_init").setValue(String.valueOf(usage.getInit())).build();
                Types.Label maxLabel = Types.Label.newBuilder().setName("jvm_mem_pool_max").setValue(String.valueOf(usage.getMax())).build();

                Types.TimeSeries.Builder usedTimeSeries = Types.TimeSeries.newBuilder();
                usedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_mem_pool_used").build());
                usedTimeSeries.addLabels(nameLabel);
                usedTimeSeries.addLabels(typeLabel);
                usedTimeSeries.addLabels(initLabel);
                usedTimeSeries.addLabels(maxLabel);
                usedTimeSeries.addLabels(gcThresholdLabel);
                usedTimeSeries.addLabels(thresholdLabel);
                usedTimeSeries.addAllLabels(labels);
                usedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(usage.getUsed()).build());
                writeRequest.addTimeseries(usedTimeSeries);

                Types.TimeSeries.Builder committedTimeSeries = Types.TimeSeries.newBuilder();
                committedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_mem_pool_committed").build());
                committedTimeSeries.addLabels(nameLabel);
                committedTimeSeries.addLabels(typeLabel);
                committedTimeSeries.addLabels(initLabel);
                committedTimeSeries.addLabels(maxLabel);
                committedTimeSeries.addLabels(gcThresholdLabel);
                committedTimeSeries.addLabels(thresholdLabel);
                committedTimeSeries.addAllLabels(labels);
                committedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(usage.getCommitted()).build());
                writeRequest.addTimeseries(committedTimeSeries);
            }

            MemoryUsageInfo collectionUsage = pool.getCollectionUsage();
            if (collectionUsage != null) {
                Types.Label initLabel = Types.Label.newBuilder().setName("jvm_mem_pool_gc_init").setValue(String.valueOf(collectionUsage.getInit())).build();
                Types.Label maxLabel = Types.Label.newBuilder().setName("jvm_mem_pool_gc_max").setValue(String.valueOf(collectionUsage.getMax())).build();

                Types.TimeSeries.Builder usedTimeSeries = Types.TimeSeries.newBuilder();
                usedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_mem_pool_gc_used").build());
                usedTimeSeries.addLabels(nameLabel);
                usedTimeSeries.addLabels(typeLabel);
                usedTimeSeries.addLabels(initLabel);
                usedTimeSeries.addLabels(maxLabel);
                usedTimeSeries.addLabels(gcThresholdLabel);
                usedTimeSeries.addLabels(thresholdLabel);
                usedTimeSeries.addAllLabels(labels);
                usedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(collectionUsage.getUsed()).build());
                writeRequest.addTimeseries(usedTimeSeries);

                Types.TimeSeries.Builder committedTimeSeries = Types.TimeSeries.newBuilder();
                committedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_mem_pool_gc_committed").build());
                committedTimeSeries.addLabels(nameLabel);
                committedTimeSeries.addLabels(typeLabel);
                committedTimeSeries.addLabels(initLabel);
                committedTimeSeries.addLabels(maxLabel);
                committedTimeSeries.addLabels(gcThresholdLabel);
                committedTimeSeries.addLabels(thresholdLabel);
                committedTimeSeries.addAllLabels(labels);
                committedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(collectionUsage.getCommitted()).build());
                writeRequest.addTimeseries(committedTimeSeries);
            }

            Types.TimeSeries.Builder overCountTimeSeries = Types.TimeSeries.newBuilder();
            overCountTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_mem_pool_over_count").build());
            overCountTimeSeries.addLabels(nameLabel);
            overCountTimeSeries.addLabels(typeLabel);
            overCountTimeSeries.addLabels(gcThresholdLabel);
            overCountTimeSeries.addLabels(thresholdLabel);
            overCountTimeSeries.addAllLabels(labels);
            overCountTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(pool.getUsageThresholdCount()).build());
            writeRequest.addTimeseries(overCountTimeSeries);

            Types.TimeSeries.Builder gcOverCountTimeSeries = Types.TimeSeries.newBuilder();
            gcOverCountTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_mem_pool_gc_over_count").build());
            gcOverCountTimeSeries.addLabels(nameLabel);
            gcOverCountTimeSeries.addLabels(typeLabel);
            gcOverCountTimeSeries.addLabels(gcThresholdLabel);
            gcOverCountTimeSeries.addLabels(thresholdLabel);
            gcOverCountTimeSeries.addAllLabels(labels);
            gcOverCountTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(pool.getCollectionUsageThresholdCount()).build());
            writeRequest.addTimeseries(gcOverCountTimeSeries);
        }
    }

    /**
     * 组装JVM线程数据到Prometheus结构
     *
     * @param thread       JVM线程信息
     * @param timestamp    统计时间戳
     * @param labels       通用标签
     * @param writeRequest Request
     */
    private static void packJvmThread(JvmThreadInfo thread, long timestamp, List<Types.Label> labels,
                                      Remote.WriteRequest.Builder writeRequest) {
        if (thread == null) {
            return;
        }

        Types.TimeSeries.Builder threadLockedTimeSeries = Types.TimeSeries.newBuilder();
        threadLockedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_thread_locked").build());
        threadLockedTimeSeries.addAllLabels(labels);
        threadLockedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(thread.getDeadlockedThreads().length).build());
        writeRequest.addTimeseries(threadLockedTimeSeries);

        Types.TimeSeries.Builder threadPeekTimeSeries = Types.TimeSeries.newBuilder();
        threadPeekTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_thread_peek").build());
        threadPeekTimeSeries.addAllLabels(labels);
        threadPeekTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(thread.getPeakThreadCount()).build());
        writeRequest.addTimeseries(threadPeekTimeSeries);

        Types.TimeSeries.Builder threadTimeSeries = Types.TimeSeries.newBuilder();
        threadTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_thread").build());
        threadTimeSeries.addAllLabels(labels);
        threadTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(thread.getThreadCount()).build());
        writeRequest.addTimeseries(threadTimeSeries);

        Types.TimeSeries.Builder threadStartedTimeSeries = Types.TimeSeries.newBuilder();
        threadStartedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_thread_started").build());
        threadStartedTimeSeries.addAllLabels(labels);
        threadStartedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(thread.getTotalStartedThreadCount()).build());
        writeRequest.addTimeseries(threadStartedTimeSeries);

        Types.TimeSeries.Builder threadDaemonTimeSeries = Types.TimeSeries.newBuilder();
        threadDaemonTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_thread_daemon").build());
        threadDaemonTimeSeries.addAllLabels(labels);
        threadDaemonTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(thread.getDaemonThreadCount()).build());
        writeRequest.addTimeseries(threadDaemonTimeSeries);

        Map<State, Integer> stateCount = thread.getStateCount();
        for (Entry<State, Integer> entry : stateCount.entrySet()) {
            Types.TimeSeries.Builder threadStateTimeSeries = Types.TimeSeries.newBuilder();
            threadStateTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("jvm_thread_" + entry.getKey().name()).build());
            threadStateTimeSeries.addAllLabels(labels);
            threadStateTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(entry.getValue()).build());
            writeRequest.addTimeseries(threadStateTimeSeries);
        }
    }

}
