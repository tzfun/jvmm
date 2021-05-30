package org.beifengtz.jvmm.server.channel;

import org.beifengtz.jvmm.convey.channel.JvmmSocketChannel;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;

import java.nio.channels.SocketChannel;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:27 2021/5/17
 *
 * @author beifengtz
 */
public class LogicSocketChannel extends JvmmSocketChannel {
    public LogicSocketChannel(ServerLogicSocketChannel parent, SocketChannel ch) {
        super(parent, ch);
    }

    @Override
    public void handleRequest(JvmmRequest reqMsg) {

    }

    @Override
    public void handleIdle() {
        close();
    }

    @Override
    public void cleanup() {

    }
}
