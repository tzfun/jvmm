package org.beifengtz.jvmm.server.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;
import org.beifengtz.jvmm.convey.GlobalStatus;
import org.beifengtz.jvmm.convey.GlobalType;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.core.DefaultJvmmScheduleService;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.JvmmScheduleService;
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

import java.io.Closeable;
import java.io.IOException;
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
public class CollectController implements Closeable {

    private static final int DEFAULT_TIMER_DELAY = 3;
    private static final int DEFAULT_TIMER_TIMES = 10;

    private JvmmScheduleService memoryCollectTimer;
    private JvmmScheduleService systemDynamicCollectTimer;
    private JvmmScheduleService threadDynamicCollectTimer;

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
    public List<String> getThreadInfo(JsonObject data, Channel channel, String type) {
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

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_TIMER_COLLECT_MEMORY_INFO)
    public void timerGetMemoryInfo(JsonObject data, String type, Channel channel, EventExecutor executor) {

        int gapSeconds = DEFAULT_TIMER_DELAY;
        int times = DEFAULT_TIMER_TIMES;

        if (data != null) {
            if (data.has("delay")) {
                gapSeconds = data.get("delay").getAsInt();
            }
            if (data.has("times")) {
                times = data.get("times").getAsInt();
            }
        }

        if (memoryCollectTimer == null) {
            memoryCollectTimer = new DefaultJvmmScheduleService("CollectMemory", executor);
        }

        Runnable task = () -> {
            JvmmResponse response = JvmmResponse.create().setType(type)
                    .setStatus(GlobalStatus.JVMM_STATUS_OK)
                    .setData(JvmmFactory.getCollector().getMemory().toJson());
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(response.serialize());
            } else {
                stopMemoryCollectTimer();
            }
        };
        memoryCollectTimer.setTask(task)
                .setTimes(times)
                .setTimeGap(gapSeconds)
                .setStopOnError(true)
                .start();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_STOP_TIMER_COLLECT_MEMORY_INFO)
    public void stopMemoryCollectTimer() {
        if (memoryCollectTimer != null) {
            memoryCollectTimer.stop();
        }
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_TIMER_COLLECT_SYSTEM_DYNAMIC_INFO)
    public void timerGetSystemDynamicInfo(JsonObject data, String type, Channel channel, EventExecutor executor) {

        int gapSeconds = DEFAULT_TIMER_DELAY;
        int times = DEFAULT_TIMER_TIMES;
        if (data != null) {
            if (data.has("delay")) {
                gapSeconds = data.get("delay").getAsInt();
            }
            if (data.has("times")) {
                times = data.get("times").getAsInt();
            }
        }

        if (systemDynamicCollectTimer == null) {
            systemDynamicCollectTimer = new DefaultJvmmScheduleService("CollectSystemDynamic", executor);
        }

        Runnable task = () -> {
            JvmmResponse response = JvmmResponse.create().setType(type)
                    .setStatus(GlobalStatus.JVMM_STATUS_OK)
                    .setData(JvmmFactory.getCollector().getSystemDynamic().toJson());
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(response.serialize());
            } else {
                stopSystemDynamicCollectTimer();
            }
        };
        systemDynamicCollectTimer.setTask(task)
                .setTimes(times)
                .setTimeGap(gapSeconds)
                .setStopOnError(true)
                .start();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_STOP_TIMER_COLLECT_SYSTEM_DYNAMIC_INFO)
    public void stopSystemDynamicCollectTimer() {
        if (systemDynamicCollectTimer != null) {
            systemDynamicCollectTimer.stop();
        }
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_TIMER_COLLECT_THREAD_INFO)
    public void timerGetThreadDynamicInfo(JsonObject data, String type, Channel channel, EventExecutor executor) {
        int gapSeconds = DEFAULT_TIMER_DELAY;
        int times = DEFAULT_TIMER_TIMES;

        if (data != null) {
            if (data.has("delay")) {
                gapSeconds = data.get("delay").getAsInt();
            }
            if (data.has("times")) {
                times = data.get("times").getAsInt();
            }
        }

        if (threadDynamicCollectTimer == null) {
            threadDynamicCollectTimer = new DefaultJvmmScheduleService("CollectThreadDynamic", executor);
        }

        Runnable task = () -> {
            JvmmResponse response = JvmmResponse.create().setType(type)
                    .setStatus(GlobalStatus.JVMM_STATUS_OK)
                    .setData(JvmmFactory.getCollector().getThreadDynamic().toJson());
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(response.serialize());
            } else {
                stopThreadDynamicTimer();
            }
        };
        threadDynamicCollectTimer.setTask(task)
                .setTimes(times)
                .setTimeGap(gapSeconds)
                .setStopOnError(true)
                .start();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_STOP_TIMER_COLLECT_THREAD_INFO)
    public void stopThreadDynamicTimer() {
        if (threadDynamicCollectTimer != null) {
            threadDynamicCollectTimer.stop();
        }
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_DUMP_THREAD_INFO)
    public JsonArray dumpThreadInfo() {
        String[] dump = JvmmFactory.getCollector().dumpAllThreads();
        JsonArray result = new JsonArray(dump.length);
        for (String info : dump) {
            result.add(info);
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        if (memoryCollectTimer != null) {
            memoryCollectTimer.stop();
        }
        if (threadDynamicCollectTimer != null) {
            threadDynamicCollectTimer.stop();
        }
        if (systemDynamicCollectTimer != null) {
            systemDynamicCollectTimer.stop();
        }
    }
}
