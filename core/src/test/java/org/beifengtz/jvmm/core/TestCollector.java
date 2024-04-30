package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.common.factory.ExecutorFactory;
import org.beifengtz.jvmm.common.util.IPUtil;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.core.driver.OSDriver;
import org.beifengtz.jvmm.core.entity.info.CPUInfo;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryInfo;
import org.beifengtz.jvmm.core.entity.info.SysMemInfo;
import org.beifengtz.jvmm.core.entity.info.ThreadTimedInfo;
import org.junit.jupiter.api.Test;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:58 2021/5/12
 *
 * @author beifengtz
 */
public class TestCollector {
    @Test
    public void testJsonParser() {
        JvmmCollector collector = JvmmFactory.getCollector();
        JvmMemoryInfo info = collector.getJvmMemory();
        System.err.println(info.toJsonStr());
        System.err.println(info);
    }

    @Test
    public void testParseFree() {
        String output = "Mem:        8170260     5654384      598400       12160     1917476     2198144";
        String[] split = output.split("\\s+");
        System.out.println(Arrays.toString(split));
    }

    @Test
    public void testIp() {
        System.out.println(IPUtil.getLocalIP());
        System.out.println(System.getProperties().getProperty("user.name"));
    }

    @Test
    public void testOshi() throws Exception {
        OSDriver osDriver = OSDriver.get();
        System.out.println(osDriver.getDiskInfo());
        System.out.println(osDriver.getOsFileInfo());
        osDriver.getCPUInfo(1, TimeUnit.SECONDS).thenAccept(System.out::println);
        osDriver.getNetInfo().thenAccept(System.out::println);
        osDriver.getDiskIOInfo().thenAccept(System.out::println);
        Thread.sleep(2000);
    }

    @Test
    public void testThreadPool() {
        JvmmCollector collector = JvmmFactory.getCollector();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        System.out.println(collector.getThreadPoolInfo(executor));

        System.out.println(collector.getThreadPoolInfo(ExecutorFactory.class.getName(), "SCHEDULE_THREAD_POOL"));
        ExecutorFactory.getThreadPool();
        System.out.println(collector.getThreadPoolInfo(ExecutorFactory.class.getName(), "SCHEDULE_THREAD_POOL"));
    }

    @Test
    public void testThreadDetail() {

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        System.out.println(Arrays.toString(threadMXBean.getAllThreadIds()));
        createMultiThread();
        System.out.println(Arrays.toString(threadMXBean.getAllThreadIds()));
    }

    private void createMultiThread() {
        System.out.println("\n Create multi thread");
        Thread thread = new Thread(() -> {
            System.out.println("run thread " + Thread.currentThread().getId());
        });
        thread.start();

        thread = new Thread(() -> {
            System.out.println("run thread " + Thread.currentThread().getId());
        });
        thread.start();

        thread = new Thread(() -> {
            System.out.println("run thread " + Thread.currentThread().getId());
        });
        thread.start();

        thread = new Thread(() -> {
            System.out.println("run thread " + Thread.currentThread().getId());
        });
        thread.start();
    }

    @Test
    public void calculateThreadInfo() throws InterruptedException, ExecutionException {
        JvmmCollector collector = JvmmFactory.getCollector();

        startDeadLoopThread();
        List<ThreadTimedInfo> infos = collector.getOrderedThreadTimedInfo(3, TimeUnit.SECONDS).get();
        for (ThreadTimedInfo info : infos) {
            System.out.println(info);
        }

        List<String> stacks = collector.getOrderedThreadTimedStack(3, TimeUnit.SECONDS).get();
        for (String stack : stacks) {
            System.out.println(stack);
        }
    }

    private void startDeadLoopThread() {
        Thread thread = new Thread(() -> {
            while (true) {
            }
        });
        thread.start();
        System.out.println("started loop thread " + thread.getId());
    }

    @Test
    public void testSystemMemory() {
        JvmmCollector collector = JvmmFactory.getCollector();
        SysMemInfo sysMem = collector.getSysMem();
        System.out.println(100 * (double) sysMem.getFreePhysical() / sysMem.getTotalPhysical());

        System.out.println(StringUtil.formatByteSizeGracefully(sysMem.getTotalPhysical()));
        System.out.println(StringUtil.formatByteSizeGracefully(sysMem.getFreePhysical()));
        System.out.println(StringUtil.formatByteSizeGracefully(Runtime.getRuntime().totalMemory()) + " ==> " + 100.0 * Runtime.getRuntime().freeMemory() / Runtime.getRuntime().totalMemory());

        SystemInfo si = new SystemInfo();
        GlobalMemory memory = si.getHardware().getMemory();

        long available = memory.getAvailable();
        long total = memory.getTotal();
        System.out.println(memory.getPageSize());
        System.out.println(StringUtil.formatByteSizeGracefully(available) + " ==> " + 100.0 * available / total);
    }

    @Test
    public void testCpuLoad() throws Exception {
        JvmmCollector collector = JvmmFactory.getCollector();
        OSDriver osDriver = OSDriver.get();
        System.out.println(osDriver.getCPULoadAverage());
        System.out.println(collector.getCPUInfo());
        CPUInfo cpuInfo = collector.getCPU(3, TimeUnit.SECONDS).get();
        System.out.println(cpuInfo);
        System.out.println(collector.getCPUInfo());
    }
}
