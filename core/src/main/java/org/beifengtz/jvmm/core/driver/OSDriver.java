package org.beifengtz.jvmm.core.driver;

import org.beifengtz.jvmm.common.util.ExecuteNativeUtil;
import org.beifengtz.jvmm.core.entity.info.CPUInfo;
import org.beifengtz.jvmm.core.entity.info.DiskIOInfo;
import org.beifengtz.jvmm.core.entity.info.DiskInfo;
import org.beifengtz.jvmm.core.entity.info.DiskInfo.DiskPartition;
import org.beifengtz.jvmm.core.entity.info.NetInfo;
import org.beifengtz.jvmm.core.entity.info.NetInfo.NetworkIFInfo;
import org.beifengtz.jvmm.core.entity.info.SysFileInfo;
import org.beifengtz.jvmm.core.entity.result.LinuxMemResult;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.InternetProtocolStats;
import oshi.software.os.InternetProtocolStats.IPConnection;
import oshi.software.os.InternetProtocolStats.TcpState;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Description: TODO
 * <p>
 * Created in 11:10 2023/1/31
 *
 * @author beifengtz
 */
public final class OSDriver {

    private static volatile OSDriver INSTANCE;
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final SystemInfo si;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
    }

    private OSDriver() {
        System.setProperty("oshi.os.windows.loadaverage", "true");
        si = new SystemInfo();
    }

    public static OSDriver get() {
        if (INSTANCE == null) {
            synchronized (OSDriver.class) {
                if (INSTANCE == null) {
                    INSTANCE = new OSDriver();
                }
            }
        }
        return INSTANCE;
    }

    public OperatingSystem getOS() {
        return si.getOperatingSystem();
    }

    public GlobalMemory getOSMemory() {
        return si.getHardware().getMemory();
    }

    public List<DiskInfo> getDiskInfo() {
        List<HWDiskStore> hwDisks = si.getHardware().getDiskStores();
        List<DiskInfo> disks = new ArrayList<>(hwDisks.size());

        for (HWDiskStore hwDisk : hwDisks) {
            DiskInfo disk = DiskInfo.create()
                    .setName(hwDisk.getName().replaceAll("\\\\|\\.", ""))
                    .setModel(hwDisk.getModel())
                    .setSize(hwDisk.getSize())
                    .setCurrentQueueLength(hwDisk.getCurrentQueueLength());
            for (HWPartition partition : hwDisk.getPartitions()) {
                disk.addPartition(DiskPartition.create()
                        .setSize(partition.getSize())
                        .setMount(partition.getMountPoint())
                        .setIdentification(partition.getIdentification()));
            }
            disks.add(disk);
        }
        return disks;
    }

    /**
     * 获取磁盘信息，包含磁盘的名字、模式、挂载分区、IO读写、大小、磁盘队列等
     *
     * @return {@link DiskIOInfo} list future
     */
    public List<DiskIOInfo> getDiskIOInfo() {
        List<HWDiskStore> preHwDisks = si.getHardware().getDiskStores();
        List<DiskIOInfo> disks = new ArrayList<>(preHwDisks.size());
        for (HWDiskStore disk : preHwDisks) {
            long reads = disk.getReads();
            long readBytes = disk.getReadBytes();
            long writes = disk.getWrites();
            long writeBytes = disk.getWriteBytes();

            DiskIOInfo info = DiskIOInfo.create()
                    .setName(disk.getName().replaceAll("[\\\\.]", ""))
                    .setCurrentQueueLength(disk.getCurrentQueueLength())
                    .setReads(reads)
                    .setReadBytes(readBytes)
                    .setWrites(writes)
                    .setWriteBytes(writeBytes);
            disks.add(info);
        }
        return disks;
    }

    /**
     * 获取指定磁盘的IO信息
     *
     * @param name 磁盘名
     * @return {@link DiskIOInfo} 信息，如果未找到对应磁盘名返回null
     */
    public DiskIOInfo getDiskIOInfo(String name) {
        List<HWDiskStore> hwDisks = si.getHardware().getDiskStores();
        HWDiskStore disk = hwDisks.stream().filter(o -> o.getName().contains(name)).findAny().orElse(null);
        if (disk == null) {
            return null;
        }
        return DiskIOInfo.create()
                .setName(disk.getName().replaceAll("[\\\\.]", ""))
                .setCurrentQueueLength(disk.getCurrentQueueLength())
                .setReads(disk.getReads())
                .setReadBytes(disk.getReadBytes())
                .setWrites(disk.getWrites())
                .setWriteBytes(disk.getWriteBytes());
    }

    /**
     * 获取各个磁盘分区的使用情况，包含名字、分区类型、标签、总大小、总空闲、可用大小等
     *
     * @return {@link SysFileInfo}列表
     */
    public List<SysFileInfo> getOsFileInfo() {
        FileSystem fs = si.getOperatingSystem().getFileSystem();
        List<OSFileStore> fileStores = fs.getFileStores();

        List<SysFileInfo> infos = new ArrayList<>(fileStores.size());

        for (OSFileStore store : fileStores) {
            infos.add(SysFileInfo.create()
                    .setName(store.getName())
                    .setMount(store.getMount())
                    .setType(store.getType())
                    .setLabel(store.getLabel())
                    .setSize(store.getTotalSpace())
                    .setFree(store.getFreeSpace())
                    .setUsable(store.getUsableSpace())
            );
        }
        return infos;
    }

    /**
     * 获取一段时间内的CPU信息，包含系统使用率、用户使用率、空闲率、IO等待率。
     *
     * @param delay 时间间隔
     * @param unit  时间单位
     * @return {@link CPUInfo} of {@link CompletableFuture}
     */
    public CompletableFuture<CPUInfo> getCPUInfo(int delay, TimeUnit unit) {
        long[] prevTicks = getCpuLoadTicks();
        CompletableFuture<CPUInfo> future = new CompletableFuture<>();
        executor.schedule(() -> {
            future.complete(getCpuInfoBetweenTicks(prevTicks));
        }, delay, unit);
        return future;
    }

    /**
     * 获取系统范围的 CPU 负载滴答计数器。返回一个包含八个元素的数组，
     * 这些元素表示在用户 （0）、Nice （1）、系统 （2）、空闲 （3）、IOwait （4）、硬件中断 （IRQ） （5）、软件中断/DPC （SoftIRQ）
     * （6） 或 Steal （7） 状态中花费的毫秒数。
     * <p>
     * 使用{@link TickType}去取其中的值：
     * <pre>
     *     long[] ticks = getCpuLoadTickets();
     *
     *     long nice = ticks[TickType.NICE.getIndex()];
     *     long irq = ticks[TickType.IRQ.getIndex()];
     *     long softIrq = ticks[TickType.SOFTIRQ.getIndex()];
     *     long steal = ticks[TickType.STEAL.getIndex()];
     *     long sys = ticks[TickType.SYSTEM.getIndex()];
     *     long user = ticks[TickType.USER.getIndex()];
     *     long ioWait = ticks[TickType.IOWAIT.getIndex()];
     *     long idle = ticks[TickType.IDLE.getIndex()];
     * </pre>
     *
     * @return 一个包含 8 个长值的数组，表示在 User、Nice、System、Idle、IOwait、IRQ、SoftIRQ 和 Steal 状态下花费的时间。
     */
    public long[] getCpuLoadTicks() {
        CentralProcessor processor = si.getHardware().getProcessor();
        return processor.getSystemCpuLoadTicks();
    }

    /**
     * 根据旧的ticks获取期间CPU负载数据
     *
     * @param oldTicks 旧的ticks数据
     * @return {@link CPUInfo}
     */
    public CPUInfo getCpuInfoBetweenTicks(long[] oldTicks) {
        CentralProcessor processor = si.getHardware().getProcessor();
        CPUInfo info = CPUInfo.create().setCpuNum(processor.getLogicalProcessorCount());
        if (oldTicks == null) {
            return info;
        }
        long[] ticks = processor.getSystemCpuLoadTicks();

        long nice = ticks[TickType.NICE.getIndex()] - oldTicks[TickType.NICE.getIndex()];
        long irq = ticks[TickType.IRQ.getIndex()] - oldTicks[TickType.IRQ.getIndex()];
        long softIrq = ticks[TickType.SOFTIRQ.getIndex()] - oldTicks[TickType.SOFTIRQ.getIndex()];
        long steal = ticks[TickType.STEAL.getIndex()] - oldTicks[TickType.STEAL.getIndex()];
        long sys = ticks[TickType.SYSTEM.getIndex()] - oldTicks[TickType.SYSTEM.getIndex()];
        long user = ticks[TickType.USER.getIndex()] - oldTicks[TickType.USER.getIndex()];
        long ioWait = ticks[TickType.IOWAIT.getIndex()] - oldTicks[TickType.IOWAIT.getIndex()];
        long idle = ticks[TickType.IDLE.getIndex()] - oldTicks[TickType.IDLE.getIndex()];

        long total = nice + irq + softIrq + steal + sys + user + ioWait + idle;

        double sysRate = total > 0 ? ((double) (total - idle) / total) : 0;
        double userRate = total > 0 ? ((double) user / total) : 0;
        double ioWaitRate = total > 0 ? ((double) ioWait / total) : 0;
        double idleRate = total > 0 ? ((double) idle / total) : 0;

        return info
                .setSys(sysRate)
                .setUser(userRate)
                .setIoWait(ioWaitRate)
                .setIdle(idleRate);
    }

    /**
     * @return 一分钟内的CPU平均负载，如果无效返回负数
     */
    public double getCPULoadAverage() {
        double la = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        return la >= 0 ? la : si.getHardware().getProcessor().getSystemLoadAverage(1)[0];
    }

    /**
     * 获取Linux环境下系统内存信息
     *
     * @return {@link LinuxMemResult}
     */
    public LinuxMemResult getLinuxMemoryInfo() {
        List<String> results = ExecuteNativeUtil.execute("free -b");
        LinuxMemResult result = new LinuxMemResult();
        if (results.size() > 1) {
            String[] split = results.get(1).split("\\s+");
            if (split.length > 6) {
                result.setTotal(Long.parseLong(split[1]));
                result.setUsed(Long.parseLong(split[2]));
                result.setFree(Long.parseLong(split[3]));
                result.setShared(Long.parseLong(split[4]));
                result.setBuffCache(Long.parseLong(split[5]));
                result.setAvailable(Long.parseLong(split[6]));
            }
        }
        return result;
    }

    /**
     * 获取网卡信息，包含连接数、TCP和UDP在IPv4和IPv6连接信息、各个网卡信息（mac地址、状态、上下行速度）
     *
     * @return {@link NetInfo}
     */
    public NetInfo getNetInfo() {
        OperatingSystem os = si.getOperatingSystem();
        InternetProtocolStats ips = os.getInternetProtocolStats();
        List<IPConnection> connections = ips.getConnections();
        NetInfo info = NetInfo.create()
                .setConnections(connections.size())
                .setTcpV4(ips.getTCPv4Stats())
                .setTcpV6(ips.getTCPv6Stats())
                .setUdpV4(ips.getUDPv4Stats())
                .setUdpV6(ips.getUDPv6Stats());

        long tcpV4 = 0, tcpV6 = 0, udpV4 = 0, udpV6 = 0;
        Map<TcpState, Integer> tcpStateConnections = info.getTcpStateConnections();
        for (IPConnection connection : connections) {
            String type = connection.getType();
            if (type != null) {
                switch (type.toLowerCase()) {
                    case "tcp4":
                        tcpV4++;
                        break;
                    case "tcp6":
                        tcpV6++;
                        break;
                    case "udp4":
                        udpV4++;
                        break;
                    case "udp6":
                        udpV6++;
                        break;
                }
            }

            tcpStateConnections.compute(connection.getState(), (s, n) -> {
                if (n == null) {
                    return 1;
                }
                return n + 1;
            });
        }
        info.setTcpV4Connections(tcpV4);
        info.setTcpV6Connections(tcpV6);
        info.setUdpV4Connections(udpV4);
        info.setUdpV6Connections(udpV6);
        List<NetworkIF> networkIFs = si.getHardware().getNetworkIFs();

        for (NetworkIF networkIF : networkIFs) {
            NetworkIFInfo ifInfo = NetworkIFInfo.create()
                    .setName(networkIF.getName())
                    .setAlias(networkIF.getIfAlias())
                    .setMtu(networkIF.getMTU())
                    .setMac(networkIF.getMacaddr())
                    .setStatus(networkIF.getIfOperStatus().toString())
                    .setIpV4(networkIF.getIPv4addr())
                    .setIpV6(networkIF.getIPv6addr())
                    .setRecvBytes(networkIF.getBytesRecv())
                    .setRecvCount(networkIF.getPacketsRecv())
                    .setSentBytes(networkIF.getBytesSent())
                    .setSentCount(networkIF.getPacketsSent());
            info.addNetworkIFInfo(ifInfo);
        }
        return info;
    }
}
