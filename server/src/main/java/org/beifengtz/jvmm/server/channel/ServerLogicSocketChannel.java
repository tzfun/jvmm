package org.beifengtz.jvmm.server.channel;

import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.beifengtz.jvmm.tools.factory.LoggerFactory;
import org.slf4j.Logger;

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

    private static final Logger log = LoggerFactory.logger(ServerLogicSocketChannel.class);

    @Override
    protected int doReadMessages(List<Object> buf) throws Exception {
        SocketChannel ch = null;
        try {
            ch = javaChannel().accept();
            buf.add(new LogicSocketChannel(this, ch));
            return 1;
        } catch (Throwable t) {
            log.warn("Failed to create a new channel from an accepted socket.", t);
            if (ch != null) {
                ch.close();
            }
        }
        return 0;
    }
}
