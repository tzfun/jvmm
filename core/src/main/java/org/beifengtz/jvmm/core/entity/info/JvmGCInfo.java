package org.beifengtz.jvmm.core.entity.info;


import org.beifengtz.jvmm.common.JsonParsable;

/**
 * description: JVM GC信息
 * date 16:08 2021/5/11
 *
 * @author beifengtz
 */
public class JvmGCInfo implements JsonParsable {
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

    private JvmGCInfo(){
    }

    public static JvmGCInfo create(){
        return new JvmGCInfo();
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
