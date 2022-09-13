package org.beifengtz.jvmm.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:29 2022/9/7
 *
 * @author beifengtz
 */
public class HttpServerHandlerProvider implements HandlerProvider {

    public static final String HTTP_SERVER_HANDLER_NAME = "httpServerHandler";

    private int idleTime;
    private String name;
    private EventExecutorGroup group;

    private HttpServerHandlerProvider() {
    }

    public HttpServerHandlerProvider(int idleTime, EventExecutorGroup group) {
        this(idleTime, HTTP_SERVER_HANDLER_NAME, group);
    }

    public HttpServerHandlerProvider(int idleTime, String name, EventExecutorGroup group) {
        this.idleTime = idleTime;
        this.name = name;
        this.group = group;
    }

    @Override
    public ChannelHandler getHandler() {
        return new HttpServerHandler();
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
