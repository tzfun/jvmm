package org.beifengtz.jvmm.core.contanstant;

/**
 * description: Server提供的采集项
 * date 11:02 2023/2/2
 *
 * @author beifengtz
 */
public enum CollectionType {
    /**
     * support prometheus
     */
    process,
    disk,
    /**
     * support prometheus
     */
    disk_io,
    /**
     * support prometheus
     */
    cpu,
    /**
     * support prometheus
     */
    network,
    /**
     * support prometheus
     */
    sys,
    /**
     * support prometheus
     */
    sys_memory,
    /**
     * support prometheus
     */
    sys_file,
    port,
    /**
     * support prometheus
     */
    jvm_classloading,
    jvm_classloader,
    /**
     * support prometheus
     */
    jvm_compilation,
    /**
     * support prometheus
     */
    jvm_gc,
    /**
     * support prometheus
     */
    jvm_memory,
    jvm_memory_manager,
    /**
     * support prometheus
     */
    jvm_memory_pool,
    /**
     * support prometheus
     */
    jvm_thread,
    jvm_thread_stack,
    jvm_thread_detail,
    jvm_thread_pool;
}
