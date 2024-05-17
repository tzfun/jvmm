package org.beifengtz.jvmm.server.controller;

import com.google.gson.JsonArray;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.JvmmController;
import org.beifengtz.jvmm.convey.annotation.JvmmMapping;
import org.beifengtz.jvmm.convey.annotation.RequestBody;
import org.beifengtz.jvmm.convey.annotation.RequestParam;
import org.beifengtz.jvmm.convey.entity.ResponseFuture;
import org.beifengtz.jvmm.convey.enums.Method;
import org.beifengtz.jvmm.convey.enums.RpcType;
import org.beifengtz.jvmm.core.JvmmCollector;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.Unsafe;
import org.beifengtz.jvmm.core.contanstant.CollectionType;
import org.beifengtz.jvmm.core.entity.JvmmData;
import org.beifengtz.jvmm.core.entity.info.*;
import org.beifengtz.jvmm.server.entity.dto.ThreadInfoDTO;
import org.beifengtz.jvmm.server.service.JvmmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 5:17 下午 2021/5/30
 *
 * @author beifengtz
 */
@JvmmController
@HttpController
public class CollectController {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(CollectController.class);

    @JvmmMapping(RpcType.JVMM_COLLECT_PROCESS_INFO)
    @HttpRequest("/collect/process")
    public ProcessInfo getProcessInfo() {
        return JvmmFactory.getCollector().getProcess();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_DISK_INFO)
    @HttpRequest("/collect/disk")
    public List<DiskInfo> getDiskInfo() {
        return JvmmFactory.getCollector().getDisk();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_DISK_IO_INFO)
    @HttpRequest("/collect/disk_io")
    public List<DiskIOInfo> getDiskIOInfo() {
        return JvmmFactory.getCollector().getDiskIO();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_CPU_INFO)
    @HttpRequest("/collect/cpu")
    public CPUInfo getCPUInfo() {
        return JvmmFactory.getCollector().getCPUInfo();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_NETWORK_INFO)
    @HttpRequest("/collect/network")
    public NetInfo getNetInfo() {
        return JvmmFactory.getCollector().getNetwork();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_SYS_INFO)
    @HttpRequest("/collect/sys")
    public SysInfo getSysInfo() {
        return JvmmFactory.getCollector().getSys();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_SYS_MEMORY_INFO)
    @HttpRequest("/collect/sys/memory")
    public SysMemInfo getSysMemoryInfo() {
        return JvmmFactory.getCollector().getSysMem();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_SYS_FILE_INFO)
    @HttpRequest("/collect/sys/file")
    public List<SysFileInfo> getSysFileInfo() {
        return JvmmFactory.getCollector().getSysFile();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_JVM_CLASSLOADING_INFO)
    @HttpRequest("/collect/jvm/classloading")
    public JvmClassLoadingInfo getJvmClassLoadingInfo() {
        return JvmmFactory.getCollector().getJvmClassLoading();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_JVM_CLASSLOADER_INFO)
    @HttpRequest("/collect/jvm/classloader")
    public List<JvmClassLoaderInfo> getJvmClassLoaders() {
        return JvmmFactory.getCollector().getJvmClassLoaders();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_JVM_COMPILATION_INFO)
    @HttpRequest("/collect/jvm/compilation")
    public JvmCompilationInfo getJvmCompilationInfo() {
        return JvmmFactory.getCollector().getJvmCompilation();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_JVM_GC_INFO)
    @HttpRequest("/collect/jvm/gc")
    public List<JvmGCInfo> getJvmGCInfo() {
        return JvmmFactory.getCollector().getJvmGC();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_JVM_MEMORY_MANAGER_INFO)
    @HttpRequest("/collect/jvm/memory_manager")
    public List<JvmMemoryManagerInfo> getJvmMemoryManagerInfo() {
        return JvmmFactory.getCollector().getJvmMemoryManager();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_JVM_MEMORY_POOL_INFO)
    @HttpRequest("/collect/jvm/memory_pool")
    public List<JvmMemoryPoolInfo> getJvmMemoryPoolInfo() {
        return JvmmFactory.getCollector().getJvmMemoryPool();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_JVM_MEMORY_INFO)
    @HttpRequest("/collect/jvm/memory")
    public JvmMemoryInfo getJvmMemoryInfo() {
        return JvmmFactory.getCollector().getJvmMemory();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_JVM_THREAD_INFO)
    @HttpRequest("/collect/jvm/thread")
    public JvmThreadInfo getJvmThreadInfo() {
        return JvmmFactory.getCollector().getJvmThread();
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_JVM_THREAD_STACK)
    @HttpRequest(value = "/collect/jvm/thread_stack", method = Method.POST)
    public List<String> getJvmThreadStack(@RequestBody ThreadInfoDTO data) {
        if (data == null) {
            throw new IllegalArgumentException("Missing data");
        }

        if (data.getIdArr() == null || data.getIdArr().length == 0) {
            throw new IllegalArgumentException("Missing a parameter 'id' of type list");
        }

        String[] infos = JvmmFactory.getCollector().getJvmThreadStack(data.getIdArr(), data.getDepth());
        return new ArrayList<>(Arrays.asList(infos));
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_JVM_THREAD_DETAIL)
    @HttpRequest("/collect/jvm/thread_detail")
    public JvmThreadDetailInfo[] getJvmThreadDetail(@RequestParam long[] id) {
        if (id == null || id.length == 0) {
            return JvmmFactory.getCollector().getAllJvmThreadDetailInfo();
        } else {
            return JvmmFactory.getCollector().getJvmThreadDetailInfo(id);
        }
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_JVM_DUMP_THREAD)
    @HttpRequest("/collect/jvm/dump_thread")
    public JsonArray jvmDumpThread() {
        String[] dump = JvmmFactory.getCollector().dumpAllThreads();
        JsonArray result = new JsonArray();
        for (String info : dump) {
            if (info != null) {
                result.add(info);
            }
        }
        return result;
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_JVM_THREAD_ORDERED_CPU_TIME)
    @HttpRequest("/collect/jvm/thread_ordered_cpu_time")
    public void getJvmThreadOrderedCpuTime(@RequestParam String type,
                                           @RequestParam int durationSeconds,
                                           ResponseFuture future) {
        assert durationSeconds > 0;
        JvmmCollector collector = JvmmFactory.getCollector();
        if ("stack".equals(type)) {
            collector.getOrderedThreadTimedStack(durationSeconds, TimeUnit.SECONDS).thenAccept(future::apply);
        } else {
            collector.getOrderedThreadTimedInfo(durationSeconds, TimeUnit.SECONDS).thenAccept(future::apply);
        }
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_JVM_THREAD_POOL)
    @HttpRequest("/collect/jvm/thread_pool")
    public ThreadPoolInfo getThreadPoolInfo(@RequestParam int classLoaderHash, @RequestParam String clazz,
                                            @RequestParam String instanceField, @RequestParam String field) {
        ClassLoader classLoader = null;
        if (classLoaderHash != 0) {
            classLoader = Unsafe.getClassLoader(classLoaderHash);
            if (classLoader == null) {
                logger.debug("Can not found target ClassLoader by hashcode: {}", classLoaderHash);
            }
        }
        ThreadPoolInfo info = null;
        if (StringUtil.isEmpty(instanceField)) {
            info = JvmmFactory.getCollector().getThreadPoolInfo(classLoader, clazz, field);
        } else {
            info = JvmmFactory.getCollector().getThreadPoolInfo(classLoader, clazz, instanceField, field);
        }

        if (info == null) {
            throw new IllegalArgumentException("Target thread pool is null or is not a ThreadPoolExecutor instance");
        }
        return info;
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_PORT_STATUS)
    @HttpRequest("/collect/port")
    public PortInfo getPortStatus(@RequestParam int[] ports) {
        if (ports == null) {
            throw new IllegalArgumentException("Missing required param 'ports'");
        }
        return JvmmFactory.getCollector().getPortInfo(ports);
    }

    @JvmmMapping(RpcType.JVMM_COLLECT_BATCH)
    @HttpRequest(value = "/collect/by_options")
    public JvmmData collectBatch(@RequestParam List<CollectionType> options) {
        return JvmmFactory.getCollector().collectByOptions(options, null, null);
    }
}
