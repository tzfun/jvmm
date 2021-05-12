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
public class GarbageCollectorInfo implements JsonParsable {
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

    private GarbageCollectorInfo(){
    }

    public static GarbageCollectorInfo create(){
        return new GarbageCollectorInfo();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public long getCollectionCount() {
        return collectionCount;
    }

    public void setCollectionCount(long collectionCount) {
        this.collectionCount = collectionCount;
    }

    public long getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(long collectionTime) {
        this.collectionTime = collectionTime;
    }

    public String[] getMemoryPoolNames() {
        return memoryPoolNames;
    }

    public void setMemoryPoolNames(String[] memoryPoolNames) {
        this.memoryPoolNames = memoryPoolNames;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
