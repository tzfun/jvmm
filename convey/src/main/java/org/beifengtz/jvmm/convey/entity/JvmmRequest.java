package org.beifengtz.jvmm.convey.entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.beifengtz.jvmm.common.JsonParsable;
import org.beifengtz.jvmm.convey.GlobalType;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 19:00 2021/5/17
 *
 * @author beifengtz
 */
public class JvmmRequest implements JsonParsable {
    private String type;
    private JsonElement data;

    private JvmmRequest(){}

    public static JvmmRequest create(){
        return new JvmmRequest();
    }

    public static JvmmRequest parseFrom(String msg){
        return new Gson().fromJson(msg, JvmmRequest.class);
    }

    public boolean isHeartbeat() {
        return GlobalType.JVMM_TYPE_HEARTBEAT.name().equalsIgnoreCase(type);
    }

    public String getType() {
        return type;
    }

    public JvmmRequest setType(String type) {
        this.type = type;
        return this;
    }

    public JvmmRequest setType(GlobalType type) {
        this.type = type.name();
        return this;
    }

    public JsonElement getData() {
        return data;
    }

    public JvmmRequest setData(JsonElement data) {
        this.data = data;
        return this;
    }
}
