package org.beifengtz.jvmm.convey.enums;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 8:42 下午 2021/5/17
 *
 * @author beifengtz
 */
public enum GlobalType {
    /**
     * 默认协议
     */
    JVMM_TYPE_HANDLE_MSG,
    /**
     * 心跳包
     */
    JVMM_TYPE_HEARTBEAT,
    /**
     * 安全认证
     */
    JVMM_TYPE_BUBBLE,
    JVMM_TYPE_AUTHENTICATION,
    /**
     * ping pong联通测试，服务发现也可用此协议
     */
    JVMM_TYPE_PING,
    JVMM_TYPE_PONG,

    /**
     * 关闭目标Jvmm服务器（并不是宿主服务）
     */
    JVMM_TYPE_SERVER_SHUTDOWN,

    /**
     * 执行类协议
     */
    JVMM_TYPE_EXECUTE_GC,
    JVMM_TYPE_EXECUTE_SET_CLASSLOADING_VERBOSE,
    JVMM_TYPE_EXECUTE_SET_MEMORY_VERBOSE,
    JVMM_TYPE_EXECUTE_SET_THREAD_CPU_TIME_ENABLED,
    JVMM_TYPE_EXECUTE_SET_THREAD_CONTENTION_MONITOR_ENABLED,
    JVMM_TYPE_EXECUTE_RESET_PEAK_THREAD_COUNT,
    JVMM_TYPE_EXECUTE_JAVA_PROCESS,
    JVMM_TYPE_EXECUTE_JVM_TOOL,
    JVMM_TYPE_EXECUTE_JAD,

    /**
     * 信息采集类协议
     */
    JVMM_TYPE_COLLECT_SYSTEM_STATIC_INFO,
    JVMM_TYPE_COLLECT_PROCESS_INFO,
    JVMM_TYPE_COLLECT_CLASSLOADING_INFO,
    JVMM_TYPE_COLLECT_COMPILATION_INFO,
    JVMM_TYPE_COLLECT_GARBAGE_COLLECTOR_INFO,
    JVMM_TYPE_COLLECT_MEMORY_MANAGER_INFO,
    JVMM_TYPE_COLLECT_MEMORY_POOL_INFO,
    JVMM_TYPE_COLLECT_MEMORY_INFO,
    JVMM_TYPE_COLLECT_SYSTEM_DYNAMIC_INFO,
    JVMM_TYPE_COLLECT_THREAD_INFO,
    JVMM_TYPE_COLLECT_THREAD_DYNAMIC_INFO,
    JVMM_TYPE_DUMP_THREAD_INFO,

    /**
     * Web端批量采集
     */
    JVMM_TYPE_COLLECT_BATCH,

    /**
     * async-profiler数据采样相关协议
     */
    JVMM_TYPE_PROFILER_EXECUTE,
    JVMM_TYPE_PROFILER_SAMPLE,
}
