package org.beifengtz.jvmm.core.entity.result;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 14:20 2022/10/20
 *
 * @author beifengtz
 */
public class OsDiskIO implements JsonParsable {

    /**
     * 磁盘IO使用率
     */
    private float usage;

    /**
     * 读吞吐率
     */
    private float readPerSecond;

    /**
     * 写吞吐率
     */
    private float writePerSecond;

    public float getUsage() {
        return usage;
    }

    public OsDiskIO setUsage(float usage) {
        this.usage = usage;
        return this;
    }

    public float getReadPerSecond() {
        return readPerSecond;
    }

    public OsDiskIO setReadPerSecond(float readPerSecond) {
        this.readPerSecond = readPerSecond;
        return this;
    }

    public float getWritePerSecond() {
        return writePerSecond;
    }

    public OsDiskIO setWritePerSecond(float writePerSecond) {
        this.writePerSecond = writePerSecond;
        return this;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
