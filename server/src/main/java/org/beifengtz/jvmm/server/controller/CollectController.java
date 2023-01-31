package org.beifengtz.jvmm.server.controller;

import com.google.gson.JsonArray;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.JvmmController;
import org.beifengtz.jvmm.convey.annotation.JvmmMapping;
import org.beifengtz.jvmm.convey.annotation.RequestBody;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.convey.enums.Method;
import org.beifengtz.jvmm.convey.handler.HttpChannelHandler;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.entity.info.*;
import org.beifengtz.jvmm.server.entity.conf.CollectOptions;
import org.beifengtz.jvmm.server.entity.dto.JvmmDataDTO;
import org.beifengtz.jvmm.server.entity.dto.ThreadInfoDTO;
import org.beifengtz.jvmm.server.service.JvmmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_CPU_INFO)
    @HttpRequest("/collect/cpu")
    public void getCPUInfo(ChannelHandlerContext ctx, HttpChannelHandler handler) {
        JvmmFactory.getCollector().getCPU(info -> handler.response(ctx, HttpResponseStatus.OK, info.toString()));
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_NETWORK_INFO)
    @HttpRequest("/collect/network")
    public void getNetInfo(ChannelHandlerContext ctx, HttpChannelHandler handler) {
        JvmmFactory.getCollector().getNetwork(info -> handler.response(ctx, HttpResponseStatus.OK, info.toString()));
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

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_JVM_DUMP_THREAD)
    @HttpRequest("/collect/jvm/dump_thread")
    public JsonArray jvmDumpThread() {
        String[] dump = JvmmFactory.getCollector().dumpAllThreads();
        JsonArray result = new JsonArray(dump.length);
        for (String info : dump) {
            result.add(info);
        }
        return result;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_BATCH)
    @HttpRequest(value = "/collect/by_options", method = Method.POST)
    public JvmmDataDTO collectBatch(@RequestBody CollectOptions options) {
        return JvmmService.collectByOptions(options);
    }
}
