package org.beifengtz.jvmm.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:16 2021/5/17
 *
 * @author beifengtz
 */
public class JvmmServerHandlerProvider implements HandlerProvider {

    public static final String JVMM_SERVER_HANDLER_NAME = "jvmmServerHandler";

    private int idleTime;
    private String name;
    private EventExecutorGroup group;

    private JvmmServerHandlerProvider() {
    }

    public JvmmServerHandlerProvider(int idleTime, EventExecutorGroup group) {
        this(idleTime, JVMM_SERVER_HANDLER_NAME, group);
    }

    public JvmmServerHandlerProvider(int idleTime, String name, EventExecutorGroup group) {
        this.idleTime = idleTime;
        this.name = name;
        this.group = group;
    }

    @Override
    public ChannelHandler getHandler() {
        return new JvmmServerHandler();
    }

    @Override
    public int getReaderIdle() {
        return idleTime;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public EventExecutorGroup getGroup() {
        return group;
    }
}
