package org.beifengtz.jvmm.core.entity;

/**
 * <p>
 * Description: TODO 宿主机信息
 * </p>
 * <p>
 * Created in 15:05 2021/5/11
 *
 * @author beifengtz
 */
public class SystemStaticInfo {
    private String name;
    private String version;
    /**
     * 体系架构
     */
    private String arch;
    /**
     * 最后一分钟的系统平均负载，如果为负值表示不可用
     */
    private double loadAverage;
    /**
     * 可用处理器数量
     */
    private int availableProcessors;

    private String timeZone;
}
