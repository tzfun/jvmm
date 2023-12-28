package org.beifengtz.jvmm.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.convey.handler.HttpChannelHandler;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.entity.conf.AuthOptionConf;
import org.beifengtz.jvmm.server.entity.conf.HttpServerConf;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:37 2022/9/7
 *
 * @author beifengtz
 */
public class HttpServerHandler extends HttpChannelHandler {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(HttpServerHandler.class);

    @Override
    public InternalLogger logger() {
        return logger;
    }

    @Override
    protected boolean handleBefore(ChannelHandlerContext ctx, String uri, FullHttpRequest msg) {
        HttpServerConf conf = ServerContext.getConfiguration().getServer().getHttp();
        AuthOptionConf auth = conf.getAuth();
        if (auth.isEnable()) {
            String authStr = msg.headers().get("Authorization");
            if (StringUtil.isEmpty(authStr) || !authStr.startsWith("Basic")) {
                response401(ctx);
                return false;
            }
            try {
                String[] up = new String(Base64.getDecoder().decode(authStr.split("\\s")[1]), StandardCharsets.UTF_8).split(":");
                if (!Objects.equals(auth.getUsername(), up[0]) || !Objects.equals(auth.getPassword(), up[1])) {
                    response401(ctx);
                    return false;
                }
            } catch (Exception e) {
                response401(ctx);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void handleFinally(ChannelHandlerContext ctx, FullHttpRequest msg) {

    }
}
