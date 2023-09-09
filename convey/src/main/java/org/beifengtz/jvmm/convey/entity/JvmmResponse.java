package org.beifengtz.jvmm.convey.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.beifengtz.jvmm.common.JsonParsable;
import org.beifengtz.jvmm.common.exception.MessageSerializeException;
import org.beifengtz.jvmm.common.util.CodingUtil;
import org.beifengtz.jvmm.convey.enums.RpcStatus;
import org.beifengtz.jvmm.convey.enums.RpcType;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 19:00 2021/5/17
 *
 * @author beifengtz
 */
public class JvmmResponse implements JvmmMsg {
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
                    break;
                }
                case JvmmMsg.MSG_FLAG_DATA: {
                    byte len = msg.readByte();
                    byte[] bytes = new byte[len];
                    msg.readBytes(bytes);
                    int dataLength = CodingUtil.byteArrayToInt(bytes);
                    bytes = new byte[dataLength];
                    msg.readBytes(bytes);
                    response.setData(JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)));
                    break;
                }
                default: {
                    throw new MessageSerializeException("Can not parse jvmm message field with wrong flag");
                }
            }
        }

        return response;
    }

    public ByteBuf serialize() {
        if (type == null) {
            throw new MessageSerializeException("Missing required param: 'type'");
        }
        if (status == null) {
            status = RpcStatus.JVMM_STATUS_OK;
        }
        CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer();

        //  消息体标志
        compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(new byte[]{JvmmMsg.MSG_FLAG_RESPONSE}));

        //  消息类型标志
        byte[] typeBytes = CodingUtil.intToAtomicByteArray(type.getValue());
        compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(new byte[]{JvmmMsg.MSG_FLAG_TYPE, (byte) typeBytes.length}));
        compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(typeBytes));

        //  上下文ID标志
        byte[] contextIdBytes = CodingUtil.longToByteArray(contextId);
        compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(new byte[]{JvmmMsg.MSG_FLAG_CONTEXT_ID, (byte) contextIdBytes.length}));
        compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(contextIdBytes));

        //  消息状态码标志
        byte[] statusBytes = CodingUtil.intToAtomicByteArray(status.getValue());
        compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(new byte[]{JvmmMsg.MSG_FLAG_STATUS, (byte) statusBytes.length}));
        compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(statusBytes));

        //  消息标志
        if (message != null) {
            byte[] messageBytes = data.toString().getBytes(StandardCharsets.UTF_8);
            byte[] lenBytes = CodingUtil.intToAtomicByteArray(messageBytes.length);
            compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(new byte[]{JvmmMsg.MSG_FLAG_MESSAGE, (byte) lenBytes.length}));
            compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(lenBytes));
            compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(messageBytes));
        }

        //  数据标志
        if (data != null) {
            byte[] dataBytes = data.toString().getBytes(StandardCharsets.UTF_8);
            byte[] lenBytes = CodingUtil.intToAtomicByteArray(dataBytes.length);
            compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(new byte[]{JvmmMsg.MSG_FLAG_DATA, (byte) lenBytes.length}));
            compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(lenBytes));
            compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(dataBytes));
        }
        return compositeByteBuf;
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
