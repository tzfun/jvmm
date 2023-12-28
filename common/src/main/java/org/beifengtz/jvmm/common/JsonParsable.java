package org.beifengtz.jvmm.common;

import com.google.gson.JsonObject;
import org.beifengtz.jvmm.common.util.StringUtil;

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

    static <T> T parseFrom(String json, Class<T> clazz) {
        return StringUtil.getGson().fromJson(json, clazz);
    }

    default String toJsonStr() {
        return StringUtil.getGson().toJson(this);
    }

    default JsonObject toJson() {
        return StringUtil.getGson().toJsonTree(this).getAsJsonObject();
    }
}
