package org.beifengtz.jvmm.convey.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import org.beifengtz.jvmm.common.JsonParsable;
import org.beifengtz.jvmm.common.exception.MessageSerializeException;
import org.beifengtz.jvmm.common.util.CodingUtil;
import org.beifengtz.jvmm.convey.enums.RpcStatus;
import org.beifengtz.jvmm.convey.enums.RpcType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 19:00 2021/5/17
 *
 * @author beifengtz
 */
public class JvmmResponse implements JvmmMsg, JsonParsable {
    private RpcType type;
    private RpcStatus status;
    private String message;
    private JsonElement data;
    private long contextId;

    private JvmmResponse() {
    }

    public static JvmmResponse create() {
        return new JvmmResponse();
    }

    public static JvmmResponse parseFrom(ByteBuf msg) {
        if (msg.readByte() != JvmmMsg.MSG_FLAG_RESPONSE) {
            throw new MessageSerializeException("Can not parse jvmm response with wrong flag");
        }
        JvmmResponse response = new JvmmResponse();
        while (msg.isReadable()) {
            byte flag = msg.readByte();
            switch (flag) {
                case JvmmMsg.MSG_FLAG_TYPE: {
                    byte len = msg.readByte();
                    byte[] bytes = new byte[len];
                    msg.readBytes(bytes);
                    response.setType(RpcType.valueOf(CodingUtil.byteArrayToInt(bytes)));
                    break;
                }
                case JvmmMsg.MSG_FLAG_CONTEXT_ID: {
                    byte len = msg.readByte();
                    byte[] bytes = new byte[len];
                    msg.readBytes(bytes);
                    response.setContextId(CodingUtil.byteArrayToLong(bytes));
                    break;
                }
                case JvmmMsg.MSG_FLAG_STATUS: {
                    byte len = msg.readByte();
                    byte[] bytes = new byte[len];
                    msg.readBytes(bytes);
                    response.setStatus(RpcStatus.valueOf(CodingUtil.byteArrayToInt(bytes)));
                    break;
                }
                case JvmmMsg.MSG_FLAG_MESSAGE: {
                    byte len = msg.readByte();
                    byte[] bytes = new byte[len];
                    msg.readBytes(bytes);
                    int messageLength = CodingUtil.byteArrayToInt(bytes);
                    bytes = new byte[messageLength];
                    msg.readBytes(bytes);
                    response.setMessage(new String(bytes, StandardCharsets.UTF_8));
                }
                case JvmmMsg.MSG_FLAG_DATA: {
                    byte len = msg.readByte();
                    byte[] bytes = new byte[len];
                    msg.readBytes(bytes);
                    int dataLength = CodingUtil.byteArrayToInt(bytes);
                    bytes = new byte[dataLength];
                    msg.readBytes(bytes);
                    response.setData(JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)));
                }
                default: {
                    throw new MessageSerializeException("Can not parse jvmm message field with wrong flag");
                }
            }
        }

        return response;
    }

    public byte[] serialize() {
        if (type == null) {
            throw new MessageSerializeException("Missing required param: 'type'");
        }
        if (status == null) {
            status = RpcStatus.JVMM_STATUS_OK;
        }

        int length = 1;
        List<byte[]> buffer = new ArrayList<>();
        //  消息体标志
        buffer.add(new byte[]{JvmmMsg.MSG_FLAG_RESPONSE});

        //  消息类型标志
        byte[] typeBytes = CodingUtil.intToAtomicByteArray(type.getValue());
        length += 2;
        buffer.add(new byte[]{JvmmMsg.MSG_FLAG_TYPE, (byte) typeBytes.length});
        length += typeBytes.length;
        buffer.add(typeBytes);

        //  上下文ID标志
        byte[] contextIdBytes = CodingUtil.longToByteArray(contextId);
        length += 2;
        buffer.add(new byte[]{JvmmMsg.MSG_FLAG_CONTEXT_ID, (byte) contextIdBytes.length});
        length += contextIdBytes.length;
        buffer.add(contextIdBytes);

        //  消息状态码标志
        byte[] statusBytes = CodingUtil.intToAtomicByteArray(status.getValue());
        length += 2;
        buffer.add(new byte[]{JvmmMsg.MSG_FLAG_STATUS, (byte) statusBytes.length});
        length += statusBytes.length;
        buffer.add(statusBytes);

        //  消息标志
        if (message != null) {
            byte[] messageBytes = data.toString().getBytes(StandardCharsets.UTF_8);
            byte[] lenBytes = CodingUtil.intToAtomicByteArray(messageBytes.length);
            length += 2;
            buffer.add(new byte[]{JvmmMsg.MSG_FLAG_MESSAGE, (byte) lenBytes.length});
            length += lenBytes.length;
            buffer.add(lenBytes);
            length += messageBytes.length;
            buffer.add(messageBytes);
        }

        //  数据标志
        if (data != null) {
            byte[] dataBytes = data.toString().getBytes(StandardCharsets.UTF_8);
            byte[] lenBytes = CodingUtil.intToAtomicByteArray(dataBytes.length);
            length += 2;
            buffer.add(new byte[]{JvmmMsg.MSG_FLAG_DATA, (byte) lenBytes.length});
            length += lenBytes.length;
            buffer.add(lenBytes);
            length += dataBytes.length;
            buffer.add(dataBytes);
        }

        //  合并所有bytes
        byte[] result = new byte[length];
        int idx = 0;
        for (byte[] bytes : buffer) {
            System.arraycopy(bytes, 0, result, idx, bytes.length);
            idx += bytes.length;
        }
        return result;
    }

    public RpcType getType() {
        return type;
    }

    public JvmmResponse setType(RpcType type) {
        this.type = type;
        return this;
    }

    public RpcStatus getStatus() {
        return status;
    }

    public JvmmResponse setStatus(RpcStatus status) {
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

    public long getContextId() {
        return contextId;
    }

    public JvmmResponse setContextId(long contextId) {
        this.contextId = contextId;
        return this;
    }
}
