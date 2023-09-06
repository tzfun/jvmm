package org.beifengtz.jvmm.server.controller;

import com.google.gson.JsonArray;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.JvmmController;
import org.beifengtz.jvmm.convey.annotation.JvmmMapping;
import org.beifengtz.jvmm.convey.annotation.RequestBody;
import org.beifengtz.jvmm.convey.annotation.RequestParam;
import org.beifengtz.jvmm.convey.entity.ResponseFuture;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.convey.enums.Method;
import org.beifengtz.jvmm.core.CollectionType;
import org.beifengtz.jvmm.core.JvmmCollector;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.Unsafe;
import org.beifengtz.jvmm.core.entity.info.DiskInfo;
import org.beifengtz.jvmm.core.entity.info.JvmClassLoaderInfo;
import org.beifengtz.jvmm.core.entity.info.JvmClassLoadingInfo;
import org.beifengtz.jvmm.core.entity.info.JvmCompilationInfo;
import org.beifengtz.jvmm.core.entity.info.JvmGCInfo;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryInfo;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryManagerInfo;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryPoolInfo;
import org.beifengtz.jvmm.core.entity.info.JvmThreadDetailInfo;
import org.beifengtz.jvmm.core.entity.info.JvmThreadInfo;
import org.beifengtz.jvmm.core.entity.info.PortInfo;
import org.beifengtz.jvmm.core.entity.info.ProcessInfo;
import org.beifengtz.jvmm.core.entity.info.SysFileInfo;
import org.beifengtz.jvmm.core.entity.info.SysInfo;
import org.beifengtz.jvmm.core.entity.info.SysMemInfo;
import org.beifengtz.jvmm.core.entity.info.ThreadPoolInfo;
import org.beifengtz.jvmm.server.entity.dto.ThreadInfoDTO;
import org.beifengtz.jvmm.server.service.JvmmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(CollectController.class);

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_PROCESS_INFO)
    @HttpRequest("/collect/process")
    public ProcessInfo getProcessInfo() {
        return JvmmFactory.getCollector().getProcess();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_DISK_INFO)
    @HttpRequest("/collect/disk")
    public List<DiskInfo> getDiskInfo() {
        return JvmmFactory.getCollector().getDisk();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_DISK_IO_INFO)
    @HttpRequest("/collect/disk_io")
    public void getDiskIOInfo(ResponseFuture future) {
        JvmmFactory.getCollector().getDiskIO().thenAccept(future::apply);
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_CPU_INFO)
    @HttpRequest("/collect/cpu")
    public void getCPUInfo(ResponseFuture future) {
        JvmmFactory.getCollector().getCPU().thenAccept(future::apply);
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_NETWORK_INFO)
    @HttpRequest("/collect/network")
    public void getNetInfo(ResponseFuture future) {
        JvmmFactory.getCollector().getNetwork().thenAccept(future::apply);
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_SYS_INFO)
    @HttpRequest("/collect/sys")
    public SysInfo getSysInfo() {
        return JvmmFactory.getCollector().getSys();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_SYS_MEMORY_INFO)
    @HttpRequest("/collect/sys/memory")
    public SysMemInfo getSysMemoryInfo() {
        return JvmmFactory.getCollector().getSysMem();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_SYS_FILE_INFO)
    @HttpRequest("/collect/sys/file")
    public List<SysFileInfo> getSysFileInfo() {
        return JvmmFactory.getCollector().getSysFile();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_JVM_CLASSLOADING_INFO)
    @HttpRequest("/collect/jvm/classloading")
    public JvmClassLoadingInfo getJvmClassLoadingInfo() {
        return JvmmFactory.getCollector().getJvmClassLoading();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_JVM_CLASSLOADER_INFO)
    @HttpRequest("/collect/jvm/classloader")
    public List<JvmClassLoaderInfo> getJvmClassLoaders() {
        return JvmmFactory.getCollector().getJvmClassLoaders();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_JVM_COMPILATION_INFO)
    @HttpRequest("/collect/jvm/compilation")
    public JvmCompilationInfo getJvmCompilationInfo() {
        return JvmmFactory.getCollector().getJvmCompilation();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_JVM_GC_INFO)
    @HttpRequest("/collect/jvm/gc")
    public List<JvmGCInfo> getJvmGCInfo() {
        return JvmmFactory.getCollector().getJvmGC();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_JVM_MEMORY_MANAGER_INFO)
    @HttpRequest("/collect/jvm/memory_manager")
    public List<JvmMemoryManagerInfo> getJvmMemoryManagerInfo() {
        return JvmmFactory.getCollector().getJvmMemoryManager();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_JVM_MEMORY_POOL_INFO)
    @HttpRequest("/collect/jvm/memory_pool")
    public List<JvmMemoryPoolInfo> getJvmMemoryPoolInfo() {
        return JvmmFactory.getCollector().getJvmMemoryPool();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_JVM_MEMORY_INFO)
    @HttpRequest("/collect/jvm/memory")
    public JvmMemoryInfo getJvmMemoryInfo() {
        return JvmmFactory.getCollector().getJvmMemory();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_JVM_THREAD_INFO)
    @HttpRequest("/collect/jvm/thread")
    public JvmThreadInfo getJvmThreadInfo() {
        return JvmmFactory.getCollector().getJvmThread();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_JVM_THREAD_STACK)
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

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_JVM_THREAD_DETAIL)
    @HttpRequest("/collect/jvm/thread_detail")
    public JvmThreadDetailInfo[] getJvmThreadDetail(@RequestParam long[] id) {
        if (id == null || id.length == 0) {
            return JvmmFactory.getCollector().getAllJvmThreadDetailInfo();
        } else {
            return JvmmFactory.getCollector().getJvmThreadDetailInfo(id);
        }
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_JVM_DUMP_THREAD)
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

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_JVM_THREAD_ORDERED_CPU_TIME)
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

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_JVM_THREAD_POOL)
    @HttpRequest("/collect/jvm/thread_pool")
    public ThreadPoolInfo getThreadPoolInfo(@RequestParam int classLoaderHash, @RequestParam String clazz,
                                            @RequestParam String instanceField, @RequestParam String field) {
        ClassLoader classLoader = Unsafe.getClassLoader(classLoaderHash);
        if (classLoader == null) {
            logger.debug("Can not found target ClassLoader by hashcode: {}", classLoaderHash);
        }
        if (StringUtil.isEmpty(instanceField)) {
            return JvmmFactory.getCollector().getThreadPoolInfo(classLoader, clazz, field);
        } else {
            return JvmmFactory.getCollector().getThreadPoolInfo(classLoader, clazz, instanceField, field);
        }
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_PORT_STATUS)
    @HttpRequest("/collect/port")
    public PortInfo getPortStatus(@RequestParam int[] ports) {
        if (ports == null) {
            throw new IllegalArgumentException("Missing required param 'ports'");
        }
        return JvmmFactory.getCollector().getPortInfo(ports);
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_BATCH)
    @HttpRequest(value = "/collect/by_options")
    public void collectBatch(@RequestParam List<CollectionType> options, ResponseFuture future) {
        JvmmService.collectByOptions(options, null, null, pair -> {
            if (pair.getLeft().get() <= 0) {
                future.apply(pair.getRight());
            }
        });
    }
}
