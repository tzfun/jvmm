package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:03 2021/5/11
 *
 * @author beifengtz
 */
public class JvmClassLoadingInfo implements JsonParsable {
    /**
     * 是否开启打印输出
     */
    private boolean verbose;
    private int loadedClassCount;
    private long unLoadedClassCount;
    private long totalLoadedClassCount;

    private JvmClassLoadingInfo() {
    }

    public static JvmClassLoadingInfo create() {
        return new JvmClassLoadingInfo();
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public int getLoadedClassCount() {
        return loadedClassCount;
    }

    public void setLoadedClassCount(int loadedClassCount) {
        this.loadedClassCount = loadedClassCount;
    }

    public long getUnLoadedClassCount() {
        return unLoadedClassCount;
    }

    public void setUnLoadedClassCount(long unLoadedClassCount) {
        this.unLoadedClassCount = unLoadedClassCount;
    }

    public long getTotalLoadedClassCount() {
        return totalLoadedClassCount;
    }

    public void setTotalLoadedClassCount(long totalLoadedClassCount) {
        this.totalLoadedClassCount = totalLoadedClassCount;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
