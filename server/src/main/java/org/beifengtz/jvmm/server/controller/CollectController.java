package org.beifengtz.jvmm.server.controller;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.netty.channel.Channel;
import org.beifengtz.jvmm.convey.GlobalStatus;
import org.beifengtz.jvmm.convey.GlobalType;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.core.JvmmCollector;
import org.beifengtz.jvmm.core.JvmmFactory;
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
import org.beifengtz.jvmm.server.annotation.JvmmController;
import org.beifengtz.jvmm.server.annotation.JvmmMapping;

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
public class CollectController {

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_SYSTEM_STATIC_INFO)
    public SystemStaticInfo getSystemStaticInfo() {
        return JvmmFactory.getCollector().getSystemStatic();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_CLASSLOADING_INFO)
    public ClassLoadingInfo getClassLoadingInfo() {
        return JvmmFactory.getCollector().getClassLoading();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_COMPILATION_INFO)
    public CompilationInfo getCompilationInfo() {
        return JvmmFactory.getCollector().getCompilation();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_PROCESS_INFO)
    public ProcessInfo getProcessInfo() {
        return JvmmFactory.getCollector().getProcess();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_GARBAGE_COLLECTOR_INFO)
    public List<GarbageCollectorInfo> getGarbageCollectorInfo() {
        return JvmmFactory.getCollector().getGarbageCollector();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_MEMORY_MANAGER_INFO)
    public List<MemoryManagerInfo> getMemoryManagerInfo() {
        return JvmmFactory.getCollector().getMemoryManager();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_MEMORY_POOL_INFO)
    public List<MemoryPoolInfo> getMemoryPoolInfo() {
        return JvmmFactory.getCollector().getMemoryPool();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_MEMORY_INFO)
    public MemoryInfo getMemoryInfo() {
        return JvmmFactory.getCollector().getMemory();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_SYSTEM_DYNAMIC_INFO)
    public SystemDynamicInfo getSystemDynamicInfo() {
        return JvmmFactory.getCollector().getSystemDynamic();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_THREAD_DYNAMIC_INFO)
    public ThreadDynamicInfo getThreadDynamicInfo() {
        return JvmmFactory.getCollector().getThreadDynamic();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_COLLECT_THREAD_INFO)
    public List<String> getThreadInfo(JvmmRequest req, Channel channel, String type) {
        JsonObject data = null;
        if (req.getData() == null) {
            throw new IllegalArgumentException("Missing data");
        } else {
            data = req.getData().getAsJsonObject();
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
            return ImmutableList.copyOf(infos);
        }
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_TIMER_COLLECT_MEMORY_INFO)
    public void timerGetMemoryInfo(JvmmRequest req, Channel channel) {
        if (req.getData() == null) {
            throw new IllegalArgumentException("Missing data");
        }
        JsonObject data = req.getData().getAsJsonObject();
        int gapSeconds = 3;
        int times = -1;
        if (data.has("delay")) {
            gapSeconds = data.get("delay").getAsInt();
        }
        if (data.has("times")) {
            gapSeconds = data.get("times").getAsInt();
        }
        JvmmCollector collector = JvmmFactory.getCollector();
        collector.timerGetMemory(gapSeconds, times, info -> {
            JvmmResponse response = JvmmResponse.create().setType(req.getType()).setStatus(GlobalStatus.JVMM_STATUS_OK).setData(info.toJson());
            if (channel.isActive()) {
                channel.writeAndFlush(response);
            } else {
                collector.stopTimerGetMemory();
            }
        });
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_STOP_TIMER_COLLECT_MEMORY_INFO)
    public void stopTimerGetMemoryInfo() {
        JvmmFactory.getCollector().stopTimerGetMemory();
    }
}
