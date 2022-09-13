package org.beifengtz.jvmm.server.entity.dto;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:00 2022/9/13
 *
 * @author beifengtz
 */
public class CollectOptionsDTO {
    private boolean classloading;
    private boolean compilation;
    private boolean memory;
    private boolean memoryManager;
    private boolean memoryPool;
    private boolean gc;
    private boolean system;
    private boolean systemDynamic;
    private boolean threadDynamic;

    public boolean isClassloading() {
        return classloading;
    }

    public boolean isCompilation() {
        return compilation;
    }

    public boolean isMemory() {
        return memory;
    }

    public boolean isMemoryManager() {
        return memoryManager;
    }

    public boolean isMemoryPool() {
        return memoryPool;
    }

    public boolean isGc() {
        return gc;
    }

    public boolean isSystem() {
        return system;
    }

    public boolean isSystemDynamic() {
        return systemDynamic;
    }

    public boolean isThreadDynamic() {
        return threadDynamic;
    }
}
