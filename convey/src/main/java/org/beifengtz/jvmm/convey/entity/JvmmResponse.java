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
public class JvmmResponse  implements JsonParsable {
    private String type;
    private String status;
    private String message;
    private JsonElement data;

    private JvmmResponse() {
    }

    public static JvmmResponse create() {
        return new JvmmResponse();
    }

    public String getType() {
        return type;
    }

    public JvmmResponse setType(String type) {
        this.type = type;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public JvmmResponse setStatus(String status) {
        this.status = status;
        return this;
    }

    public JsonElement getData() {
        return data;
    }

    public JvmmResponse setData(JsonElement data) {
        this.data = data;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public JvmmResponse setMessage(String message) {
        this.message = message;
        return this;
    }
}
