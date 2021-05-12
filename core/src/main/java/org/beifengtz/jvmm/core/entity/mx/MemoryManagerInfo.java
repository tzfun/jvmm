package org.beifengtz.jvmm.core.entity.mx;

import org.beifengtz.jvmm.core.entity.JsonParsable;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 15:32 2021/5/11
 *
 * @author beifengtz
 */
public class MemoryManagerInfo implements JsonParsable {
    private String name;
    private boolean valid;
    private String[] memoryPoolNames;

    private MemoryManagerInfo(){
    }

    public static MemoryManagerInfo create(){
        return new MemoryManagerInfo();
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
