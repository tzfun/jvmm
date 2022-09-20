package org.beifengtz.jvmm.server.entity.dto;

import org.beifengtz.jvmm.common.JsonParsable;
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

    private ClassLoadingInfo classloading;
    private CompilationInfo compilation;
    private List<GarbageCollectorInfo> garbageCollector;
    private MemoryInfo memory;
    private List<MemoryManagerInfo> memoryManager;
    private List<MemoryPoolInfo> memoryPool;
    private SystemDynamicInfo systemDynamic;
    private ThreadDynamicInfo thread;
    /**
     * 以下是静态数据
     */
    private ProcessInfo process;
    private SystemStaticInfo system;

    public void setNode(String node) {
        this.node = node;
    }

    public void setClassloading(ClassLoadingInfo classloading) {
        this.classloading = classloading;
    }

    public void setCompilation(CompilationInfo compilation) {
        this.compilation = compilation;
    }

    public void setGarbageCollector(List<GarbageCollectorInfo> garbageCollector) {
        this.garbageCollector = garbageCollector;
    }

    public void setMemory(MemoryInfo memory) {
        this.memory = memory;
    }

    public void setMemoryManager(List<MemoryManagerInfo> memoryManager) {
        this.memoryManager = memoryManager;
    }

    public void setMemoryPool(List<MemoryPoolInfo> memoryPool) {
        this.memoryPool = memoryPool;
    }

    public void setSystemDynamic(SystemDynamicInfo systemDynamic) {
        this.systemDynamic = systemDynamic;
    }

    public void setThread(ThreadDynamicInfo thread) {
        this.thread = thread;
    }

    public void setProcess(ProcessInfo process) {
        this.process = process;
    }

    public void setSystem(SystemStaticInfo system) {
        this.system = system;
    }

    public String getNode() {
        return node;
    }

    public ClassLoadingInfo getClassloading() {
        return classloading;
    }

    public CompilationInfo getCompilation() {
        return compilation;
    }

    public List<GarbageCollectorInfo> getGarbageCollector() {
        return garbageCollector;
    }

    public MemoryInfo getMemory() {
        return memory;
    }

    public List<MemoryManagerInfo> getMemoryManager() {
        return memoryManager;
    }

    public List<MemoryPoolInfo> getMemoryPool() {
        return memoryPool;
    }

    public SystemDynamicInfo getSystemDynamic() {
        return systemDynamic;
    }

    public ThreadDynamicInfo getThread() {
        return thread;
    }

    public ProcessInfo getProcess() {
        return process;
    }

    public SystemStaticInfo getSystem() {
        return system;
    }
}
