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
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:35 2021/5/17
 *
 * @author beifengtz
 */
public interface HandlerProvider {

    ChannelHandler getHandler();

    default int getReaderIdle() {
        return 10;
    }

    default String getName() {
        return "handler";
    }

    default EventExecutorGroup getGroup() {
        return null;
    }

    default SslContext getSslContext() {
        return null;
    }

    default long getChunkedSize() {
        return 0xFFFFF;
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
