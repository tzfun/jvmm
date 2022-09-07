package org.beifengtz.jvmm.core.conf.entity;

/**
 * Description: TODO
 *
 * Created in 17:24 2022/8/29
 *
 * @author beifengtz
 */
public class SentinelOptions {
    private boolean enableClassLoading;
    private boolean enableCompilation;
    private boolean enableGarbageCollector;
    private boolean enableMemory = true;
    private boolean enableMemoryPool;
    private boolean enableMemoryManage;
    private boolean enableSystemDynamic = true;
    private boolean enableThreadDynamic = true;

    public SentinelOptions enableAll() {
        this.enableClassLoading = true;
        this.enableCompilation = true;
        this.enableGarbageCollector = true;
        this.enableMemory = true;
        this.enableMemoryPool = true;
        this.enableMemoryManage = true;
        this.enableSystemDynamic = true;
        this.enableThreadDynamic = true;
        return this;
    }

    public boolean isEnableClassLoading() {
        return enableClassLoading;
    }

    public boolean isEnableCompilation() {
        return enableCompilation;
    }

    public boolean isEnableGarbageCollector() {
        return enableGarbageCollector;
    }

    public boolean isEnableMemory() {
        return enableMemory;
    }

    public boolean isEnableMemoryManage() {
        return enableMemoryManage;
    }

    public boolean isEnableMemoryPool() {
        return enableMemoryPool;
    }

    public boolean isEnableSystemDynamic() {
        return enableSystemDynamic;
    }

    public boolean isEnableThreadDynamic() {
        return enableThreadDynamic;
    }

    public SentinelOptions setEnableClassLoading(boolean enableClassLoading) {
        this.enableClassLoading = enableClassLoading;
        return this;
    }

    public SentinelOptions setEnableCompilation(boolean enableCompilation) {
        this.enableCompilation = enableCompilation;
        return this;
    }

    public SentinelOptions setEnableGarbageCollector(boolean enableGarbageCollector) {
        this.enableGarbageCollector = enableGarbageCollector;
        return this;
    }

    public SentinelOptions setEnableMemory(boolean enableMemory) {
        this.enableMemory = enableMemory;
        return this;
    }

    public SentinelOptions setEnableMemoryManage(boolean enableMemoryManage) {
        this.enableMemoryManage = enableMemoryManage;
        return this;
    }

    public SentinelOptions setEnableMemoryPool(boolean enableMemoryPool) {
        this.enableMemoryPool = enableMemoryPool;
        return this;
    }

    public SentinelOptions setEnableSystemDynamic(boolean enableSystemDynamic) {
        this.enableSystemDynamic = enableSystemDynamic;
        return this;
    }

    public SentinelOptions setEnableThreadDynamic(boolean enableThreadDynamic) {
        this.enableThreadDynamic = enableThreadDynamic;
        return this;
    }
}
