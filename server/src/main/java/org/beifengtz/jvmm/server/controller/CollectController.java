package org.beifengtz.jvmm.server.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.JvmmController;
import org.beifengtz.jvmm.convey.annotation.JvmmMapping;
import org.beifengtz.jvmm.convey.annotation.RequestBody;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.conf.entity.CollectOptions;
import org.beifengtz.jvmm.core.entity.mx.ClassLoadingInfo;
import org.beifengtz.jvmm.core.entity.mx.CompilationInfo;
import org.beifengtz.jvmm.core.entity.mx.GarbageCollectorInfo;
import org.beifengtz.jvmm.core.entity.mx.MemoryInfo;
import org.beifengtz.jvmm.core.entity.mx.MemoryManagerInfo;
import org.beifengtz.jvmm.core.entity.mx.MemoryPoolInfo;
import org.beifengtz.jvmm.core.entity.mx.ProcessInfo;
import org.beifengtz.jvmm.core.entity.mx.SystemDynamicInfo;
import org.beifengtz.jvmm.core.entity.mx.SystemStaticInfo;
import org.beifengtz.jvmm.core.entity.mx.ThreadDynamicInfo;
import org.beifengtz.jvmm.server.entity.dto.JvmmDataDTO;
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

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_SYSTEM_STATIC_INFO)
    @HttpRequest("/collect/system_static")
    public SystemStaticInfo getSystemStaticInfo() {
        return JvmmFactory.getCollector().getSystemStatic();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_CLASSLOADING_INFO)
    @HttpRequest("/collect/classloading")
    public ClassLoadingInfo getClassLoadingInfo() {
        return JvmmFactory.getCollector().getClassLoading();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_COMPILATION_INFO)
    @HttpRequest("/collect/compilation")
    public CompilationInfo getCompilationInfo() {
        return JvmmFactory.getCollector().getCompilation();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_PROCESS_INFO)
    @HttpRequest("/collect/process")
    public ProcessInfo getProcessInfo() {
        return JvmmFactory.getCollector().getProcess();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_GARBAGE_COLLECTOR_INFO)
    @HttpRequest("/collect/gc")
    public List<GarbageCollectorInfo> getGarbageCollectorInfo() {
        return JvmmFactory.getCollector().getGarbageCollector();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_MEMORY_MANAGER_INFO)
    @HttpRequest("/collect/memory_manager")
    public List<MemoryManagerInfo> getMemoryManagerInfo() {
        return JvmmFactory.getCollector().getMemoryManager();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_MEMORY_POOL_INFO)
    @HttpRequest("/collect/memory_pool")
    public List<MemoryPoolInfo> getMemoryPoolInfo() {
        return JvmmFactory.getCollector().getMemoryPool();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_MEMORY_INFO)
    @HttpRequest("/collect/memory")
    public MemoryInfo getMemoryInfo() {
        return JvmmFactory.getCollector().getMemory();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_SYSTEM_DYNAMIC_INFO)
    @HttpRequest("/collect/system_dynamic")
    public SystemDynamicInfo getSystemDynamicInfo() {
        return JvmmFactory.getCollector().getSystemDynamic();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_THREAD_DYNAMIC_INFO)
    @HttpRequest("/collect/thread_dynamic")
    public ThreadDynamicInfo getThreadDynamicInfo() {
        return JvmmFactory.getCollector().getThreadDynamic();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_THREAD_INFO)
    @HttpRequest("/collect/thread")
    public List<String> getThreadInfo(JsonObject data) {
        if (data == null) {
            throw new IllegalArgumentException("Missing data");
        }

        if (!data.has("id") || data.get("id").getAsJsonArray().size() == 0) {
            throw new IllegalArgumentException("Missing a parameter 'id' of type list");
        }
        JsonArray idList = data.get("id").getAsJsonArray();
        long[] ids = new long[idList.size()];
        for (int i = 0; i < idList.size(); i++) {
            ids[i] = idList.get(i).getAsLong();
        }
        int depth = 0;
        if (data.has("depth")) {
            depth = data.get("depth").getAsInt();
        }
        String[] infos = JvmmFactory.getCollector().getThreadInfo(ids, depth);
        return new ArrayList<>(Arrays.asList(infos));
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_DUMP_THREAD_INFO)
    @HttpRequest("/collect/dump_thread")
    public JsonArray dumpThreadInfo() {
        String[] dump = JvmmFactory.getCollector().dumpAllThreads();
        JsonArray result = new JsonArray(dump.length);
        for (String info : dump) {
            result.add(info);
        }
        return result;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_BATCH)
    @HttpRequest("/collect/by_options")
    public JvmmDataDTO collectBatch(@RequestBody CollectOptions options) {
        return JvmmService.collectByOptions(options);
    }
}
