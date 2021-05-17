package org.beifengtz.jvmm.convey.entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.beifengtz.jvmm.common.JsonParsable;

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
    private boolean heartbeat;
    private String type;
    private String account;
    private String password;
    private JsonElement data;

    private JvmmRequest(){}

    public static JvmmRequest parseFrom(String msg){
        return new Gson().fromJson(msg, JvmmRequest.class);
    }

    public boolean isHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(boolean heartbeat) {
        this.heartbeat = heartbeat;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }
}
