package org.beifengtz.jvmm.core.entity.mx;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * <p>
 * Description: TODO 宿主机信息
 * </p>
 * <p>
 * Created in 15:05 2021/5/11
 *
 * @author beifengtz
 */
public class SystemStaticInfo implements JsonParsable {
    private String name;
    private String version;
    /**
     * 体系架构
     */
    private String arch;
    /**
     * 可用处理器数量
     */
    private int availableProcessors;

    private String timeZone;

    private SystemStaticInfo(){}

    public static SystemStaticInfo create(){
        return new SystemStaticInfo();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public void setAvailableProcessors(int availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
