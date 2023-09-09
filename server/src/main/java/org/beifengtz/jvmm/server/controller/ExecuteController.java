package org.beifengtz.jvmm.server.controller;

import com.google.gson.JsonArray;
import org.beifengtz.jvmm.common.util.CodingUtil;
import org.beifengtz.jvmm.common.util.CommonUtil;
import org.beifengtz.jvmm.common.util.meta.PairKey;
import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.JvmmController;
import org.beifengtz.jvmm.convey.annotation.JvmmMapping;
import org.beifengtz.jvmm.convey.annotation.RequestBody;
import org.beifengtz.jvmm.convey.annotation.RequestParam;
import org.beifengtz.jvmm.convey.enums.RpcType;
import org.beifengtz.jvmm.convey.enums.Method;
import org.beifengtz.jvmm.core.JvmmExecutor;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.Unsafe;
import org.beifengtz.jvmm.core.contanstant.Switches;
import org.beifengtz.jvmm.core.entity.result.JpsResult;
import org.beifengtz.jvmm.server.ServerBootstrap;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.entity.dto.PatchDTO;
import org.beifengtz.jvmm.server.entity.vo.PatchVO;

import java.lang.instrument.ClassDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @JvmmMapping(RpcType.JVMM_EXECUTE_GC)
    @HttpRequest("/execute/gc")
    public String gc() {
        JvmmFactory.getExecutor().gc();
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(RpcType.JVMM_EXECUTE_SWITCHES_GET)
    @HttpRequest("/execute/get_switches")
    public Map<Switches, Boolean> getSwitches() {
        JvmmExecutor executor = JvmmFactory.getExecutor();
        Map<Switches, Boolean> info = new HashMap<>();
        info.put(Switches.classLoadingVerbose, executor.isClassLoadingVerbose());
        info.put(Switches.memoryVerbose, executor.isMemoryVerbose());
        info.put(Switches.threadCpuTime, executor.isThreadCpuTimeEnabled());
        info.put(Switches.threadContentionMonitoring, executor.isThreadContentionMonitoringEnabled());
        return info;
    }

    @JvmmMapping(RpcType.JVMM_EXECUTE_SWITCHES_SET)
    @HttpRequest("/execute/set_switches")
    public String setSwitches(@RequestParam Switches[] names, @RequestParam boolean open) {
        JvmmExecutor executor = JvmmFactory.getExecutor();
        for (Switches name : names) {
            if (name == Switches.classLoadingVerbose) {
                executor.setClassLoadingVerbose(open);
            } else if (name == Switches.memoryVerbose) {
                executor.setMemoryVerbose(open);
            } else if (name == Switches.threadCpuTime) {
                executor.setThreadCpuTimeEnabled(open);
            } else if (name == Switches.threadContentionMonitoring) {
                executor.setThreadContentionMonitoringEnabled(open);
            }
        }
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(RpcType.JVMM_EXECUTE_RESET_PEAK_THREAD_COUNT)
    @HttpRequest("/execute/reset_peak_thread_count")
    public String resetPeakThreadCount() {
        JvmmFactory.getExecutor().resetPeakThreadCount();
        return ServerContext.STATUS_OK;
    }

    @JvmmMapping(RpcType.JVMM_EXECUTE_JAVA_PROCESS)
    @HttpRequest("/execute/jps")
    public JsonArray listJavaProcess() throws Exception {
        PairKey<List<JpsResult>, String> pair = JvmmFactory.getExecutor().listJavaProcess();
        if (pair.getRight() == null) {
            JsonArray result = new JsonArray();
            pair.getLeft().forEach(o -> result.add(o.toJson()));
            return result;
        } else {
            throw new Exception(pair.getRight());
        }
    }

    @JvmmMapping(RpcType.JVMM_EXECUTE_JVM_TOOL)
    @HttpRequest(value = "/execute/jvm_tool", method = Method.POST)
    public Object jvmTool(@RequestBody String command) throws Exception {
        PairKey<List<String>, Boolean> pair = JvmmFactory.getExecutor().executeJvmTools(command);
        if (pair.getRight()) {
            return pair.getLeft();
        } else {
            return CommonUtil.join("\n", pair.getLeft());
        }
    }

    @JvmmMapping(RpcType.JVMM_EXECUTE_JAD)
    @HttpRequest("/execute/jad")
    public String jad(@RequestParam String className, @RequestParam String methodName) throws Throwable {
        return JvmmFactory.getExecutor().jad(ServerContext.getInstrumentation(), className, methodName);
    }

    @JvmmMapping(RpcType.JVMM_EXECUTE_LOAD_PATCH)
    @HttpRequest(value = "/execute/load_patch", method = Method.POST)
    public List<PatchVO> loadPatch(@RequestBody List<PatchDTO> patchList) throws Throwable {
        List<ClassDefinition> definitions = new ArrayList<>(patchList.size());
        List<PatchVO> resp = new ArrayList<>(patchList.size());
        for (PatchDTO patch : patchList) {
            byte[] classBytes = CodingUtil.hexStr2Bytes(patch.getHex());
            if (patch.getClassLoaderHash() == null) {
                List<Class<?>> loadedClass = Unsafe.findLoadedClasses(patch.getClassName());
                for (Class<?> clazz : loadedClass) {
                    definitions.add(new ClassDefinition(clazz, classBytes));
                    resp.add(new PatchVO().setClassName(patch.getClassName()).setClassLoaderHash(clazz.getClassLoader().hashCode()));
                }
            } else {
                Class<?> clazz = Unsafe.findLoadedClass(patch.getClassLoaderHash(), patch.getClassName());
                if (clazz != null) {
                    definitions.add(new ClassDefinition(clazz, classBytes));
                    resp.add(new PatchVO().setClassName(patch.getClassName()).setClassLoaderHash(clazz.getClassLoader().hashCode()));
                }
            }
        }
        if (ServerBootstrap.getInstance().redefineClass(definitions.toArray(new ClassDefinition[0]))) {
            return resp;
        }
        return new ArrayList<>();
    }
}
