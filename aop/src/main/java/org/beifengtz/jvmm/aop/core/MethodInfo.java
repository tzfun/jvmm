package org.beifengtz.jvmm.aop.core;

/**
 * description: TODO
 * date: 17:26 2023/6/28
 *
 * @author beifengtz
 */
public class MethodInfo {
    /**
     * 目标方法所在类的 {@link ClassLoader}
     */
    private ClassLoader classLoader;
    /**
     * 目标方法所在类的类名
     */
    private String className;
    /**
     * 目标方法名
     */
    private String methodName;
    /**
     * 目标方法描述
     */
    private String methodDesc;
    /**
     * 目标方法所在类实例对象，如果为静态方法，则为null
     */
    private Object target;
    /**
     * 目标方法传入参数
     */
    private Object[] args;
    /**
     * 上下文ID，多线程、多进程调用时会用到
     */
    private String contextId;

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    MethodInfo setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public String getClassName() {
        return className;
    }

    MethodInfo setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    MethodInfo setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    MethodInfo setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
        return this;
    }

    public Object getTarget() {
        return target;
    }

    MethodInfo setTarget(Object target) {
        this.target = target;
        return this;
    }

    public Object[] getArgs() {
        return args;
    }

    MethodInfo setArgs(Object[] args) {
        this.args = args;
        return this;
    }

    public String getContextId() {
        return contextId;
    }

    public MethodInfo setContextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    /**
     * @return 生成方法唯一识别key
     */
    public String key() {
        return className + "#" + methodName + methodDesc;
    }
}
