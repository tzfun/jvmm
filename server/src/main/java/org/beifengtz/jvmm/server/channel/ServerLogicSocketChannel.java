package org.beifengtz.jvmm.server.channel;

import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:24 2021/5/17
 *
 * @author beifengtz
 */
public class ServerLogicSocketChannel extends NioServerSocketChannel {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ServerLogicSocketChannel.class);

    @Override
    protected int doReadMessages(List<Object> buf) throws Exception {
        SocketChannel ch = null;
        try {
            ch = javaChannel().accept();
            buf.add(new LogicSocketChannel(this, ch));
            return 1;
        } catch (Throwable t) {
            logger.warn("Failed to create a new channel from an accepted socket.", t);
            if (ch != null) {
                ch.close();
            }
        }
        return 0;
    }
}
