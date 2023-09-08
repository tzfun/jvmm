package org.beifengtz.jvmm.convey.entity;

import io.netty.buffer.ByteBuf;

/**
 * description TODO
 * date 16:56 2023/9/8
 *
 * @author beifengtz
 */
public interface JvmmMsg {
    byte MSG_FLAG_REQUEST = 1;
    byte MSG_FLAG_RESPONSE = 2;
    byte MSG_FLAG_TYPE = 3;
    byte MSG_FLAG_STATUS = 4;
    byte MSG_FLAG_CONTEXT_ID = 5;
    /**
     * 需校验长度位数
     */
    byte MSG_FLAG_MESSAGE = 6;
    /**
     * 需校验长度位数
     */
    byte MSG_FLAG_DATA = 7;

    /**
     * 序列化为byte[]，规则如下：
     * 消息flag, [flag, (长度位数), 长度, type内容]...
     *
     * @return {@link ByteBuf}
     */
    ByteBuf serialize();
}
