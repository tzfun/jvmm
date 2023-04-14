package org.beifengtz.jvmm.server.enums;

/**
 * description: Server提供的采集项
 * date 11:02 2023/2/2
 * @author beifengtz
 */
public enum CollectionType {
    process,
    disk,
    disk_io,
    cpu,
    network,
    sys,
    sys_memory,
    sys_file,
    jvm_classloading,
    jvm_classloader,
    jvm_compilation,
    jvm_gc,
    jvm_memory,
    jvm_memory_manager,
    jvm_memory_pool,
    jvm_thread,
    jvm_thread_detail
    ;
}
