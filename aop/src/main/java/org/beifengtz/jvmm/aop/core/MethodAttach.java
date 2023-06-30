package org.beifengtz.jvmm.aop.core;

/**
 * description: TODO
 * date: 16:46 2023/6/28
 *
 * @author beifengtz
 */
class MethodAttach extends MethodInfo {
    private MethodListener listener;

    public MethodListener getListener() {
        return listener;
    }

    public MethodAttach setListener(MethodListener listener) {
        this.listener = listener;
        return this;
    }
}
