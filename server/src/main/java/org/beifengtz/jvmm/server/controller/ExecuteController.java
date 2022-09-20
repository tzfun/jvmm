package org.beifengtz.jvmm.server.controller;

import com.google.gson.JsonArray;
import org.beifengtz.jvmm.common.tuple.Pair;
import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.JvmmController;
import org.beifengtz.jvmm.convey.annotation.JvmmMapping;
import org.beifengtz.jvmm.convey.annotation.RequestParam;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.entity.result.JpsResult;
import org.beifengtz.jvmm.server.ServerContext;

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
@HttpController
public class ExecuteController {

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_EXECUTE_GC)
    @HttpRequest("/execute/gc")
    public String gc() {
        JvmmFactory.getExecutor().gc();
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_EXECUTE_SET_CLASSLOADING_VERBOSE)
    @HttpRequest("/execute/set_classloading_verbose")
    public String setClassLoadingVerbose(@RequestParam boolean verbose) {
        JvmmFactory.getExecutor().setClassLoadingVerbose(verbose);
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_EXECUTE_SET_MEMORY_VERBOSE)
    @HttpRequest("/execute/set_memory_verbose")
    public String setMemoryVerbose(@RequestParam boolean verbose) {
        JvmmFactory.getExecutor().setMemoryVerbose(verbose);
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_EXECUTE_SET_THREAD_CPU_TIME_ENABLED)
    @HttpRequest("/execute/set_thread_cpu_time_enabled")
    public String setThreadCpuTimeEnabled(@RequestParam boolean verbose) {
        JvmmFactory.getExecutor().setThreadCpuTimeEnabled(verbose);
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_EXECUTE_SET_THREAD_CONTENTION_MONITOR_ENABLED)
    @HttpRequest("/execute/set_thread_contention_monitor_enabled")
    public String setThreadContentionMonitoringEnabled(@RequestParam boolean verbose) {
        JvmmFactory.getExecutor().setThreadContentionMonitoringEnabled(verbose);
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_EXECUTE_RESET_PEAK_THREAD_COUNT)
    @HttpRequest("/execute/reset_peak_thread_count")
    public String resetPeakThreadCount() {
        JvmmFactory.getExecutor().resetPeakThreadCount();
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_EXECUTE_JAVA_PROCESS)
    @HttpRequest("/execute/jps")
    public JsonArray listJavaProcess() throws Exception {
        Pair<List<JpsResult>, String> pair = JvmmFactory.getExecutor().listJavaProcess();
        if (pair.getRight() == null) {
            JsonArray result = new JsonArray();
            pair.getLeft().forEach(o -> result.add(o.toJson()));
            return result;
        } else {
            throw new Exception(pair.getRight());
        }
    }
}
