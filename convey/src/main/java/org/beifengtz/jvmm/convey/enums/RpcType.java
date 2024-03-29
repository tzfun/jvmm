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
public enum RpcType {

    /**
     * 默认协议
     */
    JVMM_HANDLE_MSG(10001),
    /**
     * 心跳包
     */
    JVMM_HEARTBEAT(10002),
    /**
     * 安全认证
     */
    JVMM_BUBBLE(10003),
    JVMM_AUTHENTICATION(10004),
    /**
     * ping pong联通测试，服务发现也可用此协议
     */
    JVMM_PING(10005),

    /**
     * 关闭目标Jvmm服务器（并不是宿主服务）
     */
    JVMM_SERVER_SHUTDOWN(20001),

    /**
     * 执行类协议
     */
    JVMM_EXECUTE_GC(20002),
    JVMM_EXECUTE_SET_CLASSLOADING_VERBOSE(20003),
    JVMM_EXECUTE_SWITCHES_GET(20004),
    JVMM_EXECUTE_SWITCHES_SET(20005),
    JVMM_EXECUTE_RESET_PEAK_THREAD_COUNT(20006),
    JVMM_EXECUTE_JAVA_PROCESS(20007),
    JVMM_EXECUTE_JVM_TOOL(20008),
    JVMM_EXECUTE_JAD(20009),
    JVMM_EXECUTE_LOAD_PATCH(20010),

    /**
     * 信息采集类协议
     */
    JVMM_COLLECT_SYS_INFO(30001),
    JVMM_COLLECT_PROCESS_INFO(30002),
    JVMM_COLLECT_CPU_INFO(30003),
    JVMM_COLLECT_NETWORK_INFO(30004),
    JVMM_COLLECT_SYS_MEMORY_INFO(30005),
    JVMM_COLLECT_DISK_INFO(30006),
    JVMM_COLLECT_DISK_IO_INFO(30007),
    JVMM_COLLECT_SYS_FILE_INFO(30008),
    JVMM_COLLECT_PORT_STATUS(30009),
    JVMM_COLLECT_JVM_CLASSLOADING_INFO(30010),
    JVMM_COLLECT_JVM_CLASSLOADER_INFO(30011),
    JVMM_COLLECT_JVM_COMPILATION_INFO(30012),
    JVMM_COLLECT_JVM_GC_INFO(30013),
    JVMM_COLLECT_JVM_MEMORY_MANAGER_INFO(30014),
    JVMM_COLLECT_JVM_MEMORY_POOL_INFO(30015),
    JVMM_COLLECT_JVM_MEMORY_INFO(30016),
    JVMM_COLLECT_JVM_THREAD_STACK(30017),
    JVMM_COLLECT_JVM_THREAD_INFO(30018),
    JVMM_COLLECT_JVM_THREAD_DETAIL(30019),
    JVMM_COLLECT_JVM_DUMP_THREAD(30020),
    JVMM_COLLECT_JVM_THREAD_ORDERED_CPU_TIME(30021),
    JVMM_COLLECT_JVM_THREAD_POOL(30022),

    /**
     * Web端批量采集
     */
    JVMM_COLLECT_BATCH(31011),

    /**
     * async-profiler数据采样相关协议
     */
    JVMM_PROFILER_EXECUTE(40001),
    JVMM_PROFILER_SAMPLE(40002),
    JVMM_PROFILER_SAMPLE_START(40003),
    JVMM_PROFILER_SAMPLE_STOP(40004),
    JVMM_PROFILER_STATUS(40005),
    JVMM_PROFILER_LIST_EVENTS(40006);

    private final int value;

    RpcType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static RpcType valueOf(int value) {
        switch (value){
            case 10001: return JVMM_HANDLE_MSG;
            case 10002: return JVMM_HEARTBEAT;
            case 10003: return JVMM_BUBBLE;
            case 10004: return JVMM_AUTHENTICATION;
            case 10005: return JVMM_PING;
            case 20001: return JVMM_SERVER_SHUTDOWN;
            case 20002: return JVMM_EXECUTE_GC;
            case 20003: return JVMM_EXECUTE_SET_CLASSLOADING_VERBOSE;
            case 20004: return JVMM_EXECUTE_SWITCHES_GET;
            case 20005: return JVMM_EXECUTE_SWITCHES_SET;
            case 20006: return JVMM_EXECUTE_RESET_PEAK_THREAD_COUNT;
            case 20007: return JVMM_EXECUTE_JAVA_PROCESS;
            case 20008: return JVMM_EXECUTE_JVM_TOOL;
            case 20009: return JVMM_EXECUTE_JAD;
            case 20010: return JVMM_EXECUTE_LOAD_PATCH;
            case 30001: return JVMM_COLLECT_SYS_INFO;
            case 30002: return JVMM_COLLECT_PROCESS_INFO;
            case 30003: return JVMM_COLLECT_CPU_INFO;
            case 30004: return JVMM_COLLECT_NETWORK_INFO;
            case 30005: return JVMM_COLLECT_SYS_MEMORY_INFO;
            case 30006: return JVMM_COLLECT_DISK_INFO;
            case 30007: return JVMM_COLLECT_DISK_IO_INFO;
            case 30008: return JVMM_COLLECT_SYS_FILE_INFO;
            case 30009: return JVMM_COLLECT_PORT_STATUS;
            case 30010: return JVMM_COLLECT_JVM_CLASSLOADING_INFO;
            case 30011: return JVMM_COLLECT_JVM_CLASSLOADER_INFO;
            case 30012: return JVMM_COLLECT_JVM_COMPILATION_INFO;
            case 30013: return JVMM_COLLECT_JVM_GC_INFO;
            case 30014: return JVMM_COLLECT_JVM_MEMORY_MANAGER_INFO;
            case 30015: return JVMM_COLLECT_JVM_MEMORY_POOL_INFO;
            case 30016: return JVMM_COLLECT_JVM_MEMORY_INFO;
            case 30017: return JVMM_COLLECT_JVM_THREAD_STACK;
            case 30018: return JVMM_COLLECT_JVM_THREAD_INFO;
            case 30019: return JVMM_COLLECT_JVM_THREAD_DETAIL;
            case 30020: return JVMM_COLLECT_JVM_DUMP_THREAD;
            case 30021: return JVMM_COLLECT_JVM_THREAD_ORDERED_CPU_TIME;
            case 30022: return JVMM_COLLECT_JVM_THREAD_POOL;
            case 31011: return JVMM_COLLECT_BATCH;
            case 40001: return JVMM_PROFILER_EXECUTE;
            case 40002: return JVMM_PROFILER_SAMPLE;
            case 40003: return JVMM_PROFILER_SAMPLE_START;
            case 40004: return JVMM_PROFILER_SAMPLE_STOP;
            case 40005: return JVMM_PROFILER_STATUS;
            case 40006: return JVMM_PROFILER_LIST_EVENTS;
            default: return null;
        }
    }
}
