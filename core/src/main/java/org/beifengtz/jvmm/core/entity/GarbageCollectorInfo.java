package org.beifengtz.jvmm.core.entity;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:08 2021/5/11
 *
 * @author beifengtz
 */
public class GarbageCollectorInfo {
    private String name;
    private boolean valid;
    /**
     * 返回已发生的GC总数
     */
    private long collectionCount;
    /**
     * 累积GC时间
     */
    private long collectionTime;
    private String[] memoryPoolNames;
}
