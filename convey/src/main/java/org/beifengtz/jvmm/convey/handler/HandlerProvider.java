package org.beifengtz.jvmm.convey.handler;

import io.netty.channel.ChannelHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import org.beifengtz.jvmm.convey.concurrent.SafeEventExecutorGroup;

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

    EventExecutorGroup EXECUTOR_GROUP = new SafeEventExecutorGroup(
            2 * Runtime.getRuntime().availableProcessors(), new DefaultThreadFactory(JvmmRequestHandler.class));

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
}
