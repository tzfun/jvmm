package org.beifengtz.jvmm.server.service;

import io.netty.util.concurrent.Promise;
import org.beifengtz.jvmm.core.JvmmCollector;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.conf.entity.CollectOptions;
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

    int BIND_LIMIT_TIMES = 5;

    void start(Promise<Integer> promise);

    void stop();

    default int getPort() {
        return -1;
    }

    static JvmmDataDTO collectByOptions(CollectOptions options) {
        JvmmDataDTO res = new JvmmDataDTO();
        JvmmCollector collector = JvmmFactory.getCollector();

        if (options.isClassloading()) {
            res.setClassloading(collector.getClassLoading());
        }

        if (options.isCompilation()) {
            res.setCompilation(collector.getCompilation());
        }

        if (options.isGc()) {
            res.setGarbageCollector(collector.getGarbageCollector());
        }

        if (options.isMemory()) {
            res.setMemory(collector.getMemory());
        }

        if (options.isMemoryManager()) {
            res.setMemoryManager(collector.getMemoryManager());
        }

        if (options.isMemoryPool()) {
            res.setMemoryPool(collector.getMemoryPool());
        }

        if (options.isSystemDynamic()) {
            res.setSystemDynamic(collector.getSystemDynamic());
        }

        if (options.isThreadDynamic()) {
            res.setThread(collector.getThreadDynamic());
        }

        if (options.isSystemDynamic()) {
            res.setSystemDynamic(collector.getSystemDynamic());
        }

        //  下面是静态信息
        if (options.isSystem()) {
            res.setSystem(collector.getSystemStatic());
        }

        if (options.isProcess()) {
            res.setProcess(collector.getProcess());
        }

        return res;
    }
}
