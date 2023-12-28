package org.beifengtz.jvmm.server.handler;

import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.exception.AuthenticationFailedException;
import org.beifengtz.jvmm.common.util.SignatureUtil;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.enums.RpcType;
import org.beifengtz.jvmm.convey.handler.JvmmChannelHandler;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.entity.conf.JvmmServerConf;

import java.util.Objects;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 9:39 2021/5/18
 *
 * @author beifengtz
 */
public class JvmmServerHandler extends JvmmChannelHandler {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(JvmmServerHandler.class);

    private boolean authed = !ServerContext.getConfiguration().getServer().getJvmm().getAuth().isEnable();

    @Override
    public InternalLogger logger() {
        return logger;
    }

    @Override
    protected boolean handleBefore(ChannelHandlerContext ctx, JvmmRequest reqMsg) throws Exception {
        JvmmServerConf conf = ServerContext.getConfiguration().getServer().getJvmm();

        if (reqMsg.getType() == RpcType.JVMM_AUTHENTICATION) {
            auth(ctx, reqMsg, conf);
            return false;
        } else {
            if (conf.getAuth().isEnable() && !authed) {
                throw new AuthenticationFailedException();
            }
        }
        return true;
    }

    private void auth(ChannelHandlerContext ctx, JvmmRequest req, JvmmServerConf conf) throws Exception {
        if (conf.getAuth().isEnable()) {
            try {
                JsonObject data = req.getData().getAsJsonObject();

                String account = data.get("account").getAsString();
                String password = data.get("password").getAsString();
                if (Objects.equals(SignatureUtil.MD5(conf.getAuth().getUsername()), account)
                        && Objects.equals(SignatureUtil.MD5(conf.getAuth().getPassword()), password)) {
                    logger().debug("Auth successful. channelId: {}", ctx.channel().hashCode());
                } else {
                    throw new AuthenticationFailedException();
                }
            } catch (IllegalStateException | NullPointerException e) {
                throw new AuthenticationFailedException();
            }
        }
        authed = true;
        JvmmResponse response = JvmmResponse.create().setType(RpcType.JVMM_AUTHENTICATION);
        ctx.writeAndFlush(response);
    }

}
