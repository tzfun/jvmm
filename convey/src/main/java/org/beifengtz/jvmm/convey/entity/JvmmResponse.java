package org.beifengtz.jvmm.convey.entity;

import com.google.gson.JsonElement;
import org.beifengtz.jvmm.common.JsonParsable;
import org.beifengtz.jvmm.common.exception.MessageSerializeException;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.convey.enums.GlobalStatus;
import org.beifengtz.jvmm.convey.enums.GlobalType;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 19:00 2021/5/17
 *
 * @author beifengtz
 */
public class JvmmResponse implements JsonParsable {
    private String type;
    private String status;
    private String message;
    private JsonElement data;

    private JvmmResponse() {
    }

    public static JvmmResponse create() {
        return new JvmmResponse();
    }

    public static JvmmResponse parseFrom(String msg) {
        return StringUtil.getGson().fromJson(msg, JvmmResponse.class);
    }

    public String serialize() {
        if (type == null) {
            throw new MessageSerializeException("Missing required param: 'type'");
        }
        if (status == null) {
            throw new MessageSerializeException("Missing required param: 'status'");
        }
        return toJsonStr();
    }

    public String getType() {
        return type;
    }

    public JvmmResponse setType(String type) {
        this.type = type;
        return this;
    }

    public JvmmResponse setType(GlobalType type) {
        this.type = type.name();
        return this;
    }

    public String getStatus() {
        return status;
    }

    public JvmmResponse setStatus(String status) {
        this.status = status;
        return this;
    }

    public JvmmResponse setStatus(GlobalStatus status) {
        this.status = status.name();
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
