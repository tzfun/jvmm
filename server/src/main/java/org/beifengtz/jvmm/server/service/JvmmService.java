package org.beifengtz.jvmm.server.service;

import io.netty.util.concurrent.Promise;
import org.beifengtz.jvmm.core.JvmmCollector;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.server.entity.conf.CollectOptions;
import org.beifengtz.jvmm.server.entity.dto.JvmmDataDTO;

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

    int BIND_LIMIT_TIMES = 5;

    void start(Promise<Integer> promise);

    void shutdown();

    <T extends JvmmService> T addShutdownListener(ShutdownListener listener);

    default int getPort() {
        return -1;
    }

    static JvmmDataDTO collectByOptions(CollectOptions options) {
        JvmmDataDTO res = new JvmmDataDTO();
        JvmmCollector collector = JvmmFactory.getCollector();

        if (options.isClassloading()) {
            res.setClassloading(collector.getJvmClassLoading());
        }

        if (options.isCompilation()) {
            res.setCompilation(collector.getJvmCompilation());
        }

        if (options.isGc()) {
            res.setGarbageCollector(collector.getJvmGC());
        }

        if (options.isMemory()) {
            res.setMemory(collector.getJvmMemory());
        }

        if (options.isMemoryManager()) {
            res.setMemoryManager(collector.getJvmMemoryManager());
        }

        if (options.isMemoryPool()) {
            res.setMemoryPool(collector.getJvmMemoryPool());
        }

        if (options.isThreadDynamic()) {
            res.setThread(collector.getJvmThread());
        }

        //  下面是静态信息
        if (options.isSystem()) {
            res.setSystem(collector.getSys());
        }

        if (options.isProcess()) {
            res.setProcess(collector.getProcess());
        }

        return res;
    }
}
