package org.beifengtz.jvmm.server.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.beifengtz.jvmm.common.tuple.Pair;
import org.beifengtz.jvmm.convey.GlobalStatus;
import org.beifengtz.jvmm.convey.GlobalType;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.entity.result.JpsResult;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.annotation.JvmmController;
import org.beifengtz.jvmm.server.annotation.JvmmMapping;

import java.util.List;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 5:55 下午 2021/5/30
 *
 * @author beifengtz
 */
@JvmmController
public class ExecuteController {

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_EXECUTE_GC)
    public String gc() {
        JvmmFactory.getExecutor().gc();
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_EXECUTE_SET_CLASSLOADING_VERBOSE)
    public String setClassLoadingVerbose(JsonElement data) {
        if (data == null) {
            throw new IllegalArgumentException("Missing data");
        }

        JvmmFactory.getExecutor().setClassLoadingVerbose(data.getAsBoolean());
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_EXECUTE_SET_MEMORY_VERBOSE)
    public String setMemoryVerbose(JsonElement data) {
        if (data == null) {
            throw new IllegalArgumentException("Missing data");
        }

        JvmmFactory.getExecutor().setMemoryVerbose(data.getAsBoolean());
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_EXECUTE_SET_THREAD_CPU_TIME_ENABLED)
    public String setThreadCpuTimeEnabled(JsonElement data) {
        if (data == null) {
            throw new IllegalArgumentException("Missing data");
        }
        JvmmFactory.getExecutor().setThreadCpuTimeEnabled(data.getAsBoolean());
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_EXECUTE_SET_THREAD_CONTENTION_MONITOR_ENABLED)
    public String setThreadContentionMonitoringEnabled(JsonElement data) {
        if (data == null) {
            throw new IllegalArgumentException("Missing data");
        }
        JvmmFactory.getExecutor().setThreadContentionMonitoringEnabled(data.getAsBoolean());
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_EXECUTE_RESET_PEAK_THREAD_COUNT)
    public String resetPeakThreadCount() {
        JvmmFactory.getExecutor().resetPeakThreadCount();
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_EXECUTE_JAVA_PROCESS)
    public JvmmResponse listJavaProcess(String type) {
        Pair<List<JpsResult>, String> pair = JvmmFactory.getExecutor().listJavaProcess();
        JvmmResponse resp = JvmmResponse.create().setType(type);
        if (pair.getRight() == null) {
            JsonArray result = new JsonArray();
            pair.getLeft().forEach(o -> result.add(o.toJson()));
            resp.setStatus(GlobalStatus.JVMM_STATUS_OK).setData(result);
        } else {
            resp.setStatus(GlobalStatus.JVMM_STATUS_EXECUTE_FAILED).setMessage(pair.getRight());
        }
        return resp;
    }
}
