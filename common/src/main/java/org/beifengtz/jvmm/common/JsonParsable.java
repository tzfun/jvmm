package org.beifengtz.jvmm.common;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:56 2021/5/12
 *
 * @author beifengtz
 */
public interface JsonParsable {

    default String toJsonStr() {
        return new Gson().toJson(this);
    }

    default JsonObject toJson() {
        return new Gson().toJsonTree(this).getAsJsonObject();
    }
}
