package org.beifengtz.jvmm.aop.agent;

/**
 * description: TODO
 * date: 9:52 2023/10/13
 *
 * @author beifengtz
 */
public class AcrossThreadAgent {
    private static final ThreadLocal<String> CONTEXT_ID_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 获取调用线程的上下文ID
     *
     * @return 上下文ID
     */
    public static String getContextId() {
        return CONTEXT_ID_THREAD_LOCAL.get();
    }

    /**
     * 设置调用线程的上下文ID
     *
     * @param id 上下文ID
     */
    public static void setContextId(String id) {
        CONTEXT_ID_THREAD_LOCAL.set(id);
    }
}
