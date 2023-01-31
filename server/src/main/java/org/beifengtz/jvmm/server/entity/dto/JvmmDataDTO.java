package org.beifengtz.jvmm.server.entity.dto;

import org.beifengtz.jvmm.common.JsonParsable;
import org.beifengtz.jvmm.core.entity.info.JvmClassLoadingInfo;
import org.beifengtz.jvmm.core.entity.info.JvmCompilationInfo;
import org.beifengtz.jvmm.core.entity.info.JvmGCInfo;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryInfo;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryManagerInfo;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryPoolInfo;
import org.beifengtz.jvmm.core.entity.info.JvmThreadInfo;
import org.beifengtz.jvmm.core.entity.info.ProcessInfo;
import org.beifengtz.jvmm.core.entity.info.SysInfo;

import java.util.List;

/**
 * Description: TODO
 *
 * Created in 9:58 2022/8/30
 *
 * @author beifengtz
 */
public class JvmmDataDTO implements JsonParsable {
    private String node;

    private JvmClassLoadingInfo classloading;
    private JvmCompilationInfo compilation;
    private List<JvmGCInfo> garbageCollector;
    private JvmMemoryInfo memory;
    private List<JvmMemoryManagerInfo> memoryManager;
    private List<JvmMemoryPoolInfo> memoryPool;
    private JvmThreadInfo thread;
    /**
     * 以下是静态数据
     */
    private ProcessInfo process;
    private SysInfo system;

    public void setNode(String node) {
        this.node = node;
    }

    public void setClassloading(JvmClassLoadingInfo classloading) {
        this.classloading = classloading;
    }

    public void setCompilation(JvmCompilationInfo compilation) {
        this.compilation = compilation;
    }

    public void setGarbageCollector(List<JvmGCInfo> garbageCollector) {
        this.garbageCollector = garbageCollector;
    }

    public void setMemory(JvmMemoryInfo memory) {
        this.memory = memory;
    }

    public void setMemoryManager(List<JvmMemoryManagerInfo> memoryManager) {
        this.memoryManager = memoryManager;
    }

    public void setMemoryPool(List<JvmMemoryPoolInfo> memoryPool) {
        this.memoryPool = memoryPool;
    }

    public void setThread(JvmThreadInfo thread) {
        this.thread = thread;
    }

    public void setProcess(ProcessInfo process) {
        this.process = process;
    }

    public void setSystem(SysInfo system) {
        this.system = system;
    }

    public String getNode() {
        return node;
    }

    public JvmClassLoadingInfo getClassloading() {
        return classloading;
    }

    public JvmCompilationInfo getCompilation() {
        return compilation;
    }

    public List<JvmGCInfo> getGarbageCollector() {
        return garbageCollector;
    }

    public JvmMemoryInfo getMemory() {
        return memory;
    }

    public List<JvmMemoryManagerInfo> getMemoryManager() {
        return memoryManager;
    }

    public List<JvmMemoryPoolInfo> getMemoryPool() {
        return memoryPool;
    }

    public JvmThreadInfo getThread() {
        return thread;
    }

    public ProcessInfo getProcess() {
        return process;
    }

    public SysInfo getSystem() {
        return system;
    }
}
