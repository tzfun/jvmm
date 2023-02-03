package org.beifengtz.jvmm.demo;

import org.beifengtz.jvmm.common.util.meta.PairKey;
import org.beifengtz.jvmm.core.JvmmCollector;
import org.beifengtz.jvmm.core.JvmmExecutor;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.JvmmProfiler;
import org.beifengtz.jvmm.core.entity.info.JvmClassLoadingInfo;
import org.beifengtz.jvmm.core.entity.info.JvmCompilationInfo;
import org.beifengtz.jvmm.core.entity.info.JvmGCInfo;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryInfo;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryManagerInfo;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryPoolInfo;
import org.beifengtz.jvmm.core.entity.info.JvmThreadInfo;
import org.beifengtz.jvmm.core.entity.info.ProcessInfo;
import org.beifengtz.jvmm.core.entity.info.SysInfo;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerCounter;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerEvent;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Description: TODO
 * <p>
 * Created in 16:32 2021/12/15
 *
 * @author beifengtz
 */
public class ApiDemo {
    public static void main(String[] args) throws Exception {

        //  jvmm收集器，可以获取以下信息：
        //  操作系统：基础信息、Memory、CPU、Process信息
        //  Java虚拟机：Memory、GC、Class、Thread、Compilation信息
        JvmmCollector collector = JvmmFactory.getCollector();

        JvmMemoryInfo memory = collector.getJvmMemory();
        List<JvmMemoryManagerInfo> memoryManager = collector.getJvmMemoryManager();
        List<JvmMemoryPoolInfo> memoryPool = collector.getJvmMemoryPool();
        SysInfo systemStatic = collector.getSys();
        JvmClassLoadingInfo classLoading = collector.getJvmClassLoading();
        List<JvmGCInfo> garbageCollector = collector.getJvmGC();
        JvmCompilationInfo compilation = collector.getJvmCompilation();
        ProcessInfo process = collector.getProcess();
        JvmThreadInfo threadDynamic = collector.getJvmThread();
        String[] threadsInfo = collector.dumpAllThreads();

        //  jvmm执行器
        JvmmExecutor executor = JvmmFactory.getExecutor();

        executor.gc();
        executor.setMemoryVerbose(true);
        executor.setClassLoadingVerbose(true);
        executor.setThreadContentionMonitoringEnabled(true);
        executor.setThreadCpuTimeEnabled(true);
        executor.resetPeakThreadCount();
        PairKey<List<String>, Boolean> result = executor.executeJvmTools("jstat -gc 1102");

        //  jvmm采样器，仅支持 MacOS 和 Linux环境
        JvmmProfiler profiler = JvmmFactory.getProfiler();
        File file = new File("jvmm_test.html");
        //  采集cpu信息，持续时间10秒，输出html报告
        Future<String> future = JvmmFactory.getProfiler().sample(file, ProfilerEvent.cpu.name(), ProfilerCounter.samples, 10, TimeUnit.SECONDS);

        try {
            //  等待时间建议长于采样时间
            future.get(12, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
