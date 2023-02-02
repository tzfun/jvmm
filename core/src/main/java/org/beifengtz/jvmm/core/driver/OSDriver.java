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
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.InternetProtocolStats;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
     */
    public void getDiskIOInfo(Consumer<List<DiskIOInfo>> consumer) {
        List<HWDiskStore> preHwDisks = si.getHardware().getDiskStores();
        Map<String, HWDiskStore> diskMap = new HashMap<>(preHwDisks.size());
        for (HWDiskStore disk : preHwDisks) {
            diskMap.put(disk.getName(), disk);
        }
        executor.schedule(() -> {
            List<HWDiskStore> hwDisks = si.getHardware().getDiskStores();
            List<DiskIOInfo> disks = new ArrayList<>(hwDisks.size());
            for (HWDiskStore disk : hwDisks) {
                HWDiskStore pre = diskMap.get(disk.getName());
                if (pre == null) continue;
                DiskIOInfo info = DiskIOInfo.create()
                        .setName(disk.getName().replaceAll("\\\\|\\.", ""))
                        .setCurrentQueueLength(disk.getCurrentQueueLength())
                        .setReadPerSecond(disk.getReads() - pre.getReads())
                        .setReadBytesPerSecond(disk.getReadBytes() - pre.getReadBytes())
                        .setWritePerSecond(disk.getWrites() - pre.getWrites())
                        .setWriteBytesPerSecond(disk.getWriteBytes() - pre.getWriteBytes());
                disks.add(info);
            }
            consumer.accept(disks);
        }, 1, TimeUnit.SECONDS);
    }

    /**
     * 获取指定磁盘的IO信息
     *
     * @param name     磁盘名
     * @param consumer 计算完成时返回{@link DiskIOInfo}信息，如果未找到对应磁盘名返回null
     */
    public void getDiskIOInfo(String name, Consumer<DiskIOInfo> consumer) {
        List<HWDiskStore> hwDisks = si.getHardware().getDiskStores();
        HWDiskStore pre = hwDisks.stream().filter(o -> o.getName().contains(name)).findAny().orElse(null);
        if (pre == null) {
            consumer.accept(null);
            return;
        }
        executor.schedule(() -> {
            HWDiskStore disk = si.getHardware().getDiskStores().stream().filter(o -> o.getName().contains(name)).findAny().orElse(null);
            if (disk == null) {
                consumer.accept(null);
            } else {
                DiskIOInfo info = DiskIOInfo.create()
                        .setName(disk.getName().replaceAll("\\\\|\\.", ""))
                        .setCurrentQueueLength(disk.getCurrentQueueLength())
                        .setReadPerSecond(disk.getReads() - pre.getReads())
                        .setReadBytesPerSecond(disk.getReadBytes() - pre.getReadBytes())
                        .setWritePerSecond(disk.getWrites() - pre.getWrites())
                        .setWriteBytesPerSecond(disk.getWriteBytes() - pre.getWriteBytes());
                consumer.accept(info);
            }
        }, 1, TimeUnit.SECONDS);
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
     * 获取CPU信息，包含系统使用率、用户使用率、空闲率、IO等待率。
     */
    public void getCPUInfo(Consumer<CPUInfo> consumer) {
        CentralProcessor processor = si.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        executor.schedule(() -> {
            long[] ticks = processor.getSystemCpuLoadTicks();

            long nice = ticks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
            long irq = ticks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
            long softIrq = ticks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
            long steal = ticks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
            long sys = ticks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
            long user = ticks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
            long ioWait = ticks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
            long idle = ticks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];

            long total = nice + irq + softIrq + steal + sys + user + ioWait + idle;
            if (total == 0) {
                total = 1;
            }

            consumer.accept(CPUInfo.create()
                    .setCpuNum(processor.getLogicalProcessorCount())
                    .setSys((double) sys / total)
                    .setUser((double) user / total)
                    .setIoWait((double) ioWait / total)
                    .setIdle((double) idle / total));
        }, 1, TimeUnit.SECONDS);
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
     * @param consumer {@link NetInfo}
     */
    public void getNetInfo(Consumer<NetInfo> consumer) {
        OperatingSystem os = si.getOperatingSystem();
        InternetProtocolStats ips = os.getInternetProtocolStats();
        NetInfo info = NetInfo.create()
                .setConnections(ips.getConnections().size())
                .setTcpV4(ips.getTCPv4Stats())
                .setTcpV6(ips.getTCPv6Stats())
                .setUdpV4(ips.getUDPv4Stats())
                .setUdpV6(ips.getUDPv6Stats());
        List<NetworkIF> networkIFs = si.getHardware().getNetworkIFs();
        Map<String, NetworkIF> networkIFMap = new HashMap<>(networkIFs.size());
        for (NetworkIF networkIF : networkIFs) {
            networkIFMap.put(networkIF.getName(), networkIF);
        }

        executor.schedule(() -> {
            for (NetworkIF networkIF : si.getHardware().getNetworkIFs()) {
                NetworkIF preIF = networkIFMap.get(networkIF.getName());
                if (preIF == null) continue;
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
                        .setSentCount(networkIF.getPacketsSent())
                        .setRecvBytesPerSecond(networkIF.getBytesRecv() - preIF.getBytesRecv())
                        .setSentBytesPerSecond(networkIF.getBytesSent() - preIF.getBytesSent());
                info.addNetworkIFInfo(ifInfo);
            }
            consumer.accept(info);
        }, 1, TimeUnit.SECONDS);
    }
}
