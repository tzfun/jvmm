package org.beifengtz.jvmm.aop.core;

/**
 * description: TODO
 * date: 9:52 2023/10/13
 *
 * @author beifengtz
 */
public class ThreadLocalStore {
    private static final ThreadLocal<Attributes> CONTEXT_ID_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 获取调用线程的属性存储对象
     *
     * @return {@link Attributes}对象
     */
    public static Attributes getAttributes() {
        return CONTEXT_ID_THREAD_LOCAL.get();
    }

    /**
     * 克隆一个属性存储对象
     *
     * @return {@link Attributes}对象
     */
    public static Attributes cloneAttributes() {
        Attributes attributes = CONTEXT_ID_THREAD_LOCAL.get();
        if (attributes == null) {
            return null;
        }
        return attributes.clone();
    }

    /**
     * 设置当前线程的属性存储对象
     *
     * @param attributes {@link Attributes}对象
     */
    public static void setAttributes(Attributes attributes) {
        CONTEXT_ID_THREAD_LOCAL.set(attributes);
    }

    public static void clear() {
        CONTEXT_ID_THREAD_LOCAL.remove();
    }
}
