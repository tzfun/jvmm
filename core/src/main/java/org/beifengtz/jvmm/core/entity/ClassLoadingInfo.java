package org.beifengtz.jvmm.core.entity;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:03 2021/5/11
 *
 * @author beifengtz
 */
public class ClassLoadingInfo {
    /**
     * 是否开启打印输出
     */
    private boolean verbose;
    private int loadedClassCount;
    private int unLoadedClassCount;
    private int totalLoadedClassCount;
}
