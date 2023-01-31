package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 15:21 2021/5/11
 *
 * @author beifengtz
 */
public class SystemDynamicInfo implements JsonParsable {
    /**
     * 进程cpu时间
     */
    private long processCpuTime;
    /**
     * 系统cpu负载
     */
    private double systemCpuLoad;
    /**
     * 最后一分钟的系统平均负载，如果为负值表示不可用
     */
    private double loadAverage;

    private SystemDynamicInfo() {
    }

    public static SystemDynamicInfo create() {
        return new SystemDynamicInfo();
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
