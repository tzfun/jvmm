package org.beifengtz.jvmm.convey.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.beifengtz.jvmm.common.exception.MessageSerializeException;
import org.beifengtz.jvmm.common.util.CodingUtil;
import org.beifengtz.jvmm.convey.enums.RpcType;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 19:00 2021/5/17
 *
 * @author beifengtz
 */
public class JvmmRequest implements JvmmMsg {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger();
    private RpcType type;
    private JsonElement data;
    /**
     * context id，用于指定一次会话
     */
    private long contextId;

    private JvmmRequest() {
    }

    public static JvmmRequest create() {
        return new JvmmRequest();
    }

    public static long generateContextId() {
        return generateContextId(1);
    }

    /**
     * 8位ServerId + 32位时间戳 + 24位自增（去重）
     *
     * @param serverId 服务ID
     * @return 雪花ID
     */
    public static long generateContextId(int serverId) {
        long count = ID_COUNTER.getAndIncrement();
        if (count >= 0xFFFFFFF) {
            count = ID_COUNTER.getAndSet(1);
        }
        long sec = (System.currentTimeMillis() / 1000);
        return ((long) (serverId & 0xFF) << 56) | (sec << 24) | (count & 0xFFFFFF);
    }

    public static JvmmRequest parseFrom(ByteBuf msg) {
        if (msg.readByte() != JvmmMsg.MSG_FLAG_REQUEST) {
            throw new MessageSerializeException("Can not parse jvmm request with wrong flag");
        }
        JvmmRequest request = new JvmmRequest();
        while (msg.isReadable()) {
            byte flag = msg.readByte();
            switch (flag) {
                case JvmmMsg.MSG_FLAG_TYPE: {
                    byte len = msg.readByte();
                    byte[] bytes = new byte[len];
                    msg.readBytes(bytes);
                    request.setType(RpcType.valueOf(CodingUtil.byteArrayToInt(bytes)));
                    break;
                }
                case JvmmMsg.MSG_FLAG_CONTEXT_ID: {
                    byte len = msg.readByte();
                    byte[] bytes = new byte[len];
                    msg.readBytes(bytes);
                    request.setContextId(CodingUtil.byteArrayToLong(bytes));
                    break;
                }
                case JvmmMsg.MSG_FLAG_DATA: {
                    byte len = msg.readByte();
                    byte[] bytes = new byte[len];
                    msg.readBytes(bytes);
                    int dataLength = CodingUtil.byteArrayToInt(bytes);
                    bytes = new byte[dataLength];
                    msg.readBytes(bytes);
                    request.setData(JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)));
                    break;
                }
                default: {
                    throw new MessageSerializeException("Can not parse jvmm message field with wrong flag");
                }
            }
        }
        return request;
    }

    public ByteBuf serialize() {
        if (type == null) {
            throw new MessageSerializeException("Missing required param: 'type'");
        }
        if (contextId == 0) {
            contextId = generateContextId();
        }
        CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer();
        //  消息体标志
        compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(new byte[]{JvmmMsg.MSG_FLAG_REQUEST}));

        //  消息类型标志
        byte[] typeBytes = CodingUtil.intToAtomicByteArray(type.getValue());
        compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(new byte[]{JvmmMsg.MSG_FLAG_TYPE, (byte) typeBytes.length}));
        compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(typeBytes));

        //  上下文ID标志
        byte[] contextIdBytes = CodingUtil.longToByteArray(contextId);
        compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(new byte[]{JvmmMsg.MSG_FLAG_CONTEXT_ID, (byte) contextIdBytes.length}));
        compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(contextIdBytes));

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

    public JvmmRequest setType(RpcType type) {
        this.type = type;
        return this;
    }

    public JsonElement getData() {
        return data;
    }

    public JvmmRequest setData(JsonElement data) {
        this.data = data;
        return this;
    }

    public long getContextId() {
        return contextId;
    }

    public JvmmRequest setContextId(long contextId) {
        this.contextId = contextId;
        return this;
    }
}
