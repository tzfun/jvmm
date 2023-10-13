package org.beifengtz.jvmm.aop.agent;

import org.beifengtz.jvmm.aop.wrapper.RunnableWrapper;

/**
 * description Runnable代理类
 * date 15:21 2023/9/11
 *
 * @author beifengtz
 */
public class RunnableAgent extends RunnableWrapper {

    public RunnableAgent(Runnable runnable, String contextId) {
        super(runnable, contextId);
    }
}
