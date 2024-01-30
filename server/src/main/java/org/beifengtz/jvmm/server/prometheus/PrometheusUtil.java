package org.beifengtz.jvmm.server.prometheus;

import org.beifengtz.jvmm.common.exception.MessageSerializeException;
import org.beifengtz.jvmm.core.entity.JvmmData;
import org.beifengtz.jvmm.core.entity.info.CPUInfo;
import org.beifengtz.jvmm.core.entity.info.DiskIOInfo;
import org.beifengtz.jvmm.core.entity.info.NetInfo;
import org.beifengtz.jvmm.core.entity.info.NetInfo.NetworkIFInfo;
import org.beifengtz.jvmm.core.entity.info.ProcessInfo;
import org.beifengtz.jvmm.core.entity.info.SysInfo;
import org.beifengtz.jvmm.core.entity.info.SysMemInfo;
import org.beifengtz.jvmm.server.prometheus.Types.Label;
import org.beifengtz.jvmm.server.prometheus.Types.Sample;
import org.xerial.snappy.Snappy;
import oshi.software.os.InternetProtocolStats.TcpStats;
import oshi.software.os.InternetProtocolStats.UdpStats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        buildPrometheusProcess(data.getProcess(), now, labels, writeRequest);
        buildPrometheusSystem(data.getSys(), labels);
        buildPrometheusDiskIO(data.getDiskIO(), now, labels, writeRequest);
        buildPrometheusCpu(data.getCpu(), now, labels, writeRequest);
        buildPrometheusNetwork(data.getNetwork(), now, labels, writeRequest);
        buildPrometheusSysMem(data.getSysMem(), now, labels, writeRequest);
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
    private static void buildPrometheusProcess(ProcessInfo process, long timestamp, List<Types.Label> labels,
                                               Remote.WriteRequest.Builder writeRequest) {
        if (process == null) {
            return;
        }
        Types.TimeSeries.Builder processTimeSeries = Types.TimeSeries.newBuilder();
        labels.add(Types.Label.newBuilder().setName("p_name").setValue(process.getName()).build());
        labels.add(Types.Label.newBuilder().setName("p_id").setValue(String.valueOf(process.getPid())).build());
        labels.add(Types.Label.newBuilder().setName("p_up_time").setValue(String.valueOf(timestamp - process.getStartTime())).build());
        labels.add(Types.Label.newBuilder().setName("vm_name").setValue(process.getVmName()).build());
        labels.add(Types.Label.newBuilder().setName("vm_version").setValue(process.getVmVersion()).build());

        processTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("process_run_time").build());
        processTimeSeries.addAllLabels(labels);

        processTimeSeries.addSamples(Types.Sample.newBuilder()
                .setTimestamp(timestamp)
                .setValue(process.getUptime())
                .build());
        writeRequest.addTimeseries(processTimeSeries.build());
    }

    /**
     * 组装操作系统信息到Prometheus结构
     *
     * @param sys    操作系统信息
     * @param labels 通用标签
     */
    private static void buildPrometheusSystem(SysInfo sys, List<Types.Label> labels) {
        if (sys == null) {
            return;
        }
        labels.add(Types.Label.newBuilder().setName("os_name").setValue(sys.getName()).build());
        labels.add(Types.Label.newBuilder().setName("os_version").setValue(sys.getVersion()).build());
        labels.add(Types.Label.newBuilder().setName("os_arch").setValue(sys.getArch()).build());
        labels.add(Types.Label.newBuilder().setName("os_user").setValue(sys.getUser()).build());
    }

    /**
     * 组装磁盘IO数据到Prometheus结构
     *
     * @param disks        磁盘IO信息
     * @param timestamp    统计时间戳
     * @param labels       通用标签
     * @param writeRequest Request
     */
    private static void buildPrometheusDiskIO(List<DiskIOInfo> disks, long timestamp, List<Types.Label> labels,
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
    private static void buildPrometheusCpu(CPUInfo cpu, long timestamp, List<Types.Label> labels,
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
    private static void buildPrometheusNetwork(NetInfo network, long timestamp, List<Types.Label> labels,
                                               Remote.WriteRequest.Builder writeRequest) {
        if (network == null) {
            return;
        }

        Types.TimeSeries.Builder netConnectionsTimeSeries = Types.TimeSeries.newBuilder();
        netConnectionsTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_connections").build());
        netConnectionsTimeSeries.addAllLabels(labels);
        netConnectionsTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(network.getConnections()).build());
        writeRequest.addTimeseries(netConnectionsTimeSeries);

        buildPrometheusTcpStats(network.getTcpV4(), "net_tcp_v4", timestamp, labels, writeRequest);
        buildPrometheusTcpStats(network.getTcpV6(), "net_tcp_v6", timestamp, labels, writeRequest);
        buildPrometheusUdpStats(network.getUdpV4(), "net_udp_v4", timestamp, labels, writeRequest);
        buildPrometheusUdpStats(network.getUdpV6(), "net_udp_v6", timestamp, labels, writeRequest);

        List<NetworkIFInfo> networkIFInfos = network.getNetworkIFInfos();
        if (networkIFInfos != null) {
            for (NetworkIFInfo info : networkIFInfos) {
                Types.Label ifName = Types.Label.newBuilder().setName("net_if_name").setValue(info.getName()).build();
                Types.Label ifAlias = Types.Label.newBuilder().setName("net_if_alias").setValue(info.getAlias()).build();
                Types.Label ifMac = Types.Label.newBuilder().setName("net_if_mac").setValue(info.getMac()).build();
                Types.Label ifStatus = Types.Label.newBuilder().setName("net_if_status").setValue(info.getStatus()).build();
                Types.Label ifMtu = Types.Label.newBuilder().setName("net_if_mtu").setValue(String.valueOf(info.getMtu())).build();

                Types.TimeSeries.Builder ifSentSpeedTimeSeries = Types.TimeSeries.newBuilder();
                ifSentSpeedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_if_sent_speed").build());
                ifSentSpeedTimeSeries.addLabels(ifName);
                ifSentSpeedTimeSeries.addLabels(ifAlias);
                ifSentSpeedTimeSeries.addLabels(ifMac);
                ifSentSpeedTimeSeries.addLabels(ifMtu);
                ifSentSpeedTimeSeries.addLabels(ifStatus);
                ifSentSpeedTimeSeries.addAllLabels(labels);
                ifSentSpeedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(info.getSentBytesPerSecond()).build());
                writeRequest.addTimeseries(ifSentSpeedTimeSeries);

                Types.TimeSeries.Builder ifRecvSpeedTimeSeries = Types.TimeSeries.newBuilder();
                ifRecvSpeedTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_if_recv_speed").build());
                ifRecvSpeedTimeSeries.addLabels(ifName);
                ifRecvSpeedTimeSeries.addLabels(ifAlias);
                ifRecvSpeedTimeSeries.addLabels(ifMac);
                ifRecvSpeedTimeSeries.addLabels(ifMtu);
                ifRecvSpeedTimeSeries.addLabels(ifStatus);
                ifRecvSpeedTimeSeries.addAllLabels(labels);
                ifRecvSpeedTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(info.getRecvBytesPerSecond()).build());
                writeRequest.addTimeseries(ifRecvSpeedTimeSeries);

                Types.TimeSeries.Builder ifSentBytesTimeSeries = Types.TimeSeries.newBuilder();
                ifSentBytesTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_if_sent_bytes").build());
                ifSentBytesTimeSeries.addLabels(ifName);
                ifSentBytesTimeSeries.addLabels(ifAlias);
                ifSentBytesTimeSeries.addLabels(ifMac);
                ifSentBytesTimeSeries.addLabels(ifMtu);
                ifSentBytesTimeSeries.addLabels(ifStatus);
                ifSentBytesTimeSeries.addAllLabels(labels);
                ifSentBytesTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(info.getSentBytes()).build());
                writeRequest.addTimeseries(ifSentBytesTimeSeries);

                Types.TimeSeries.Builder ifRecvBytesTimeSeries = Types.TimeSeries.newBuilder();
                ifRecvBytesTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_if_recv_bytes").build());
                ifRecvBytesTimeSeries.addLabels(ifName);
                ifRecvBytesTimeSeries.addLabels(ifAlias);
                ifRecvBytesTimeSeries.addLabels(ifMac);
                ifRecvBytesTimeSeries.addLabels(ifMtu);
                ifRecvBytesTimeSeries.addLabels(ifStatus);
                ifRecvBytesTimeSeries.addAllLabels(labels);
                ifRecvBytesTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(info.getRecvBytes()).build());
                writeRequest.addTimeseries(ifRecvBytesTimeSeries);

                Types.TimeSeries.Builder ifSentCountTimeSeries = Types.TimeSeries.newBuilder();
                ifSentCountTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_if_sent_count").build());
                ifSentCountTimeSeries.addLabels(ifName);
                ifSentCountTimeSeries.addLabels(ifAlias);
                ifSentCountTimeSeries.addLabels(ifMac);
                ifSentCountTimeSeries.addLabels(ifMtu);
                ifSentCountTimeSeries.addLabels(ifStatus);
                ifSentCountTimeSeries.addAllLabels(labels);
                ifSentCountTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(info.getSentCount()).build());
                writeRequest.addTimeseries(ifSentCountTimeSeries);

                Types.TimeSeries.Builder ifRecvCountTimeSeries = Types.TimeSeries.newBuilder();
                ifRecvCountTimeSeries.addLabels(Types.Label.newBuilder().setName(PROMETHEUS_LABEL_NAME).setValue("net_if_recv_count").build());
                ifRecvCountTimeSeries.addLabels(ifName);
                ifRecvCountTimeSeries.addLabels(ifAlias);
                ifRecvCountTimeSeries.addLabels(ifMac);
                ifRecvCountTimeSeries.addLabels(ifMtu);
                ifRecvCountTimeSeries.addLabels(ifStatus);
                ifRecvCountTimeSeries.addAllLabels(labels);
                ifRecvCountTimeSeries.addSamples(Sample.newBuilder().setTimestamp(timestamp).setValue(info.getSentCount()).build());
                writeRequest.addTimeseries(ifRecvCountTimeSeries);
            }
        }
    }

    private static void buildPrometheusTcpStats(TcpStats stats, String namePrefix, long timestamp, List<Types.Label> labels,
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

    private static void buildPrometheusUdpStats(UdpStats stats, String namePrefix, long timestamp, List<Types.Label> labels,
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
    private static void buildPrometheusSysMem(SysMemInfo sysMem, long timestamp, List<Types.Label> labels,
                                              Remote.WriteRequest.Builder writeRequest) {
        if (sysMem == null) {
            return;
        }
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

}
