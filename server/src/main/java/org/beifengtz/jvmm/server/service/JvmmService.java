package org.beifengtz.jvmm.server.service;

import io.netty.util.concurrent.Promise;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 09:51 2022/9/7
 *
 * @author beifengtz
 */
public interface JvmmService {

    interface ShutdownListener {
        void onShutdown();
    }

    void start(Promise<Integer> promise);

    void shutdown();

    <T extends JvmmService> T addShutdownListener(ShutdownListener listener);

    default int getPort() {
        return -1;
    }

}
