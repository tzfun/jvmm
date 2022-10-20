package org.beifengtz.jvmm.core.entity.result;

import org.beifengtz.jvmm.common.JsonParsable;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: TODO
 *
 * Created in 14:39 2022/9/29
 *
 * @author beifengtz
 */
public class OsNetState implements JsonParsable {
    private int total;
    private Map<String, Integer> statusCount = new HashMap<>();

    public int getTotal() {
        return total;
    }

    public OsNetState setTotal(int total) {
        this.total = total;
        return this;
    }

    public Map<String, Integer> getStatusCount() {
        return statusCount;
    }

    public int getStatusCount(String status){
        return statusCount.getOrDefault(status, 0);
    }

    public OsNetState setStatusCount(String status, int count) {
        this.statusCount.put(status, count);
        return this;
    }

    public OsNetState setStatusCount(Map<String, Integer> statusCount) {
        this.statusCount = statusCount;
        return this;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
