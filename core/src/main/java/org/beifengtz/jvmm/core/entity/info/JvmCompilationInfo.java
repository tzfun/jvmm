package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:06 2021/5/11
 *
 * @author beifengtz
 */
public class JvmCompilationInfo implements JsonParsable {
    private String name;
    private boolean timeMonitoringSupported;
    private long totalCompilationTime;

    private JvmCompilationInfo() {
    }

    public static JvmCompilationInfo create() {
        return new JvmCompilationInfo();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTimeMonitoringSupported() {
        return timeMonitoringSupported;
    }

    public void setTimeMonitoringSupported(boolean timeMonitoringSupported) {
        this.timeMonitoringSupported = timeMonitoringSupported;
    }

    public long getTotalCompilationTime() {
        return totalCompilationTime;
    }

    public void setTotalCompilationTime(long totalCompilationTime) {
        this.totalCompilationTime = totalCompilationTime;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
