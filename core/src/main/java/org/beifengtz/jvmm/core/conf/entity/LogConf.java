package org.beifengtz.jvmm.core.conf.entity;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:44 2022/9/7
 *
 * @author beifengtz
 */
public class LogConf {
    private String level = "info";
    private boolean useJvmm;

    public String getLevel() {
        return level;
    }

    public LogConf setLevel(String level) {
        this.level = level;
        return this;
    }

    public boolean isUseJvmm() {
        return useJvmm;
    }

    public LogConf setUseJvmm(boolean useJvmm) {
        this.useJvmm = useJvmm;
        return this;
    }
}
