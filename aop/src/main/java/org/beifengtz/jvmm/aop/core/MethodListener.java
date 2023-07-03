package org.beifengtz.jvmm.aop.core;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * description: 方法监听器，当执行被增强方法时，会触发相应时点的方法
 * date: 11:29 2023/6/28
 *
 * @author beifengtz
 */
public abstract class MethodListener implements Comparable<MethodListener> {

    private final Set<MethodListener> listenerList = new TreeSet<>();

    synchronized void addListener(MethodListener methodListener) {
        listenerList.add(methodListener);
    }

    @Override
    public int compareTo(MethodListener o) {
        if (Objects.equals(this, o)) {
            return 0;
        }
        return this.hashCode() > o.hashCode() ? 1 : -1;
    }

    /**
     * 方法执行前触发
     *
     * @param info 目标方法信息{@link MethodInfo}
     * @throws Throwable 触发error
     */
    public void before(MethodInfo info) throws Throwable {
        for (MethodListener listener : listenerList) {
            try {
                listener.before(info);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 方法正常返回后触发
     *
     * @param info        目标方法信息{@link MethodInfo}
     * @param returnValue 目标方法返回值
     * @throws Throwable 通知执行过程中的异常
     */
    public void afterReturning(MethodInfo info, Object returnValue) throws Throwable {
        for (MethodListener listener : listenerList) {
            try {
                listener.afterReturning(info, returnValue);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 方法抛出异常后触发
     *
     * @param info      目标方法信息{@link MethodInfo}
     * @param throwable 目标方法执行时抛出的异常 {@link Throwable}
     * @throws Throwable 通知执行过程中的异常
     */
    public void afterThrowing(MethodInfo info, Throwable throwable) throws Throwable {
        for (MethodListener listener : listenerList) {
            try {
                listener.afterThrowing(info, throwable);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 方法结束时执行，无论正常返回还是抛出异常，都会触发此方法
     *
     * @param info      目标方法信息{@link MethodInfo}
     * @param returnVal 如果正常返回，此值为返回值；如果抛出异常，此值为 null
     * @param throwable 如果正常返回，此参数为 null；如果抛出异常，此值为异常对象
     * @throws Throwable 通知执行过程中的异常
     */
    public void after(MethodInfo info, Object returnVal, Throwable throwable) throws Throwable {
        for (MethodListener listener : listenerList) {
            try {
                listener.after(info, returnVal, throwable);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
