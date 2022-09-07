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

    int BIND_LIMIT_TIMES = 5;

    void start(Promise<Integer> promise);

    void stop();

    default int getPort() {
        return -1;
    }
}
