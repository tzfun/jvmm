package org.beifengtz.jvmm.client.handler;

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
public class LogicHandlerProvider implements HandlerProvider {

    private int idleTime;
    private String name;
    private EventExecutorGroup group;

    private LogicHandlerProvider() {
    }

    public LogicHandlerProvider(int idleTime, EventExecutorGroup group) {
        this(idleTime, "jvmmLogicHandler", group);
    }

    public LogicHandlerProvider(int idleTime, String name, EventExecutorGroup group) {
        this.idleTime = idleTime;
        this.name = name;
        this.group = group;
    }

    @Override
    public ChannelHandler getHandler() {
        return null;
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
