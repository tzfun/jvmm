package org.beifengtz.jvmm.aop.agent;

import org.beifengtz.jvmm.aop.core.ExecutorEnhancer;

/**
 * description Runnable代理类
 * date 15:21 2023/9/11
 *
 * @author beifengtz
 */
public class RunnableAgent implements Runnable {

    private final Runnable runnable;
    private final String contextId;

    public RunnableAgent(Runnable runnable, String contextId) {
        if (runnable == null) {
            throw new NullPointerException();
        }
        this.runnable = runnable;
        this.contextId = contextId;
    }

    @Override
    public void run() {
        ExecutorEnhancer.setContextId(contextId);
        try {
            runnable.run();
        } finally {
            ExecutorEnhancer.setContextId(null);
        }
    }
}
