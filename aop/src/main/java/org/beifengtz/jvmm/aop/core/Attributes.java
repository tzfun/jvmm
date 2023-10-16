package org.beifengtz.jvmm.aop.core;

/**
 * description: TODO
 * date: 16:09 2023/10/16
 *
 * @author beifengtz
 */
public class Attributes implements Cloneable {
    String traceId;
    String contextId;

    @Override
    public Attributes clone() {
        try {
            return (Attributes) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public String getTraceId() {
        return traceId;
    }

    public Attributes setTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public String getContextId() {
        return contextId;
    }

    public Attributes setContextId(String contextId) {
        this.contextId = contextId;
        return this;
    }
}