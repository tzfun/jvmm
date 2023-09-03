package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.common.factory.ExecutorFactory;
import org.beifengtz.jvmm.common.util.IPUtil;
import org.beifengtz.jvmm.core.driver.OSDriver;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryInfo;
import org.junit.jupiter.api.Test;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OSThread;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
        osDriver.getCPUInfo(System.out::println);
        System.out.println(osDriver.getCPULoadAverage());
        osDriver.getNetInfo(System.out::println);
        osDriver.getDiskIOInfo(System.out::println);

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
    public void testThreadDetail() throws InterruptedException {

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        System.out.println(Arrays.toString(threadMXBean.getAllThreadIds()));

        createMultiThread();
        System.out.println(Arrays.toString(threadMXBean.getAllThreadIds()));

        OSProcess process = new SystemInfo().getOperatingSystem().getCurrentProcess();
        List<OSThread> threads = process.getThreadDetails();
        threads.forEach(System.out::println);
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
}
