package org.beifengtz.jvmm.server.service;

import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 09:51 2022/9/15
 *
 * @author beifengtz
 */
public class ServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(JvmmServerService.class);

    private final Map<Integer, JvmmService> uniqueServices = new ConcurrentHashMap<>();
    private final Map<Integer, Thread> threadPool = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger();

    public void start(JvmmService service, Promise<Integer> promise) {
        try {
            int hash = service.hashCode();
            JvmmService s1 = uniqueServices.get(hash);
            if (s1 != null) {
                s1.shutdown();
                Thread thread = threadPool.get(hash);
                if (thread != null) {
                    thread.join(15000);
                }
            }

            execute(hash, service, promise);

        } catch (Exception e) {
            promise.tryFailure(e);
        }
    }

    public void remove(JvmmService service) {
        int hash  = service.hashCode();
        uniqueServices.remove(hash);
        Thread thread = threadPool.remove(hash);
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    public void startIfAbsent(JvmmService service, Promise<Integer> promise) {
        try {
            int hash = service.hashCode();

            if (uniqueServices.containsKey(hash)) {
                promise.trySuccess(uniqueServices.get(hash).getPort());
            } else {
                execute(hash, service, promise);
            }
        } catch (Exception e) {
            promise.tryFailure(e);
        }
    }

    void execute(int hash, JvmmService service, Promise<Integer> promise) {
        uniqueServices.put(hash, service);
        String name = "jvmm-service-" + counter.getAndIncrement();
        Thread thread = newThread(name, () -> {
            service.start(promise);
            threadPool.remove(hash);
            logger.debug("Jvmm service thread finished {}", name);
        });
        threadPool.put(hash, thread);
        thread.start();
    }

    Thread newThread(String name, Runnable runnable) {
        checkNotNull(name);
        checkNotNull(runnable);
        Thread thread = new Thread(runnable);
        thread.setName(name);
        return thread;
    }

}
