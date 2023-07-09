package org.beifengtz.jvmm.convey.handler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import io.netty.channel.ChannelHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.EventExecutorGroup;
import org.beifengtz.jvmm.common.JsonParsable;

/**
 * <p>
 * Description: Handler provider
 * </p>
 * <p>
 * Created in 16:35 2021/5/17
 *
 * @author beifengtz
 */
public interface HandlerProvider {

    /**
     * 每一个请求对应的逻辑处理实例
     *
     * @return 一个 {@link ChannelHandler}对象
     */
    ChannelHandler getHandler();

    /**
     * 当一个连接在 idle 时间内未收到或发送数据时，将断开连接
     * @return idle时间，单位秒，如果为0则用不断开，除非逻辑层面手动触发断开连接
     */
    default int getIdleSeconds() {
        return 10;
    }

    /**
     * @return 定义handler的名字，需唯一
     */
    default String getName() {
        return "handler";
    }

    /**
     * handler执行的线程池，如果为null则默认使用 work group
     * @return {@link EventExecutorGroup}
     */
    default EventExecutorGroup getGroup() {
        return null;
    }

    /**
     * @return SSL Context，用于通信加密与认证，如果为 null 则不生效
     */
    default SslContext getSslContext() {
        return null;
    }

    static JsonElement parseResult2Json(Object result) {
        if (result == null) return JsonNull.INSTANCE;
        JsonElement data = null;
        if (result instanceof Boolean) {
            data = new JsonPrimitive((Boolean) result);
        } else if (result instanceof Number) {
            data = new JsonPrimitive((Number) result);
        } else if (result instanceof String) {
            data = new JsonPrimitive((String) result);
        } else if (result instanceof Character) {
            data = new JsonPrimitive((Character) result);
        } else if (result instanceof JsonElement) {
            data = (JsonElement) result;
        } else if (result instanceof JsonParsable) {
            data = ((JsonParsable) result).toJson();
        } else {
            data = new Gson().toJsonTree(result);
        }
        return data;
    }
}
