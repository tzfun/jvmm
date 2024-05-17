package org.beifengtz.jvmm.server.service;

import io.netty.util.concurrent.Promise;
import org.beifengtz.jvmm.common.util.meta.PairKey;
import org.beifengtz.jvmm.core.JvmmCollector;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.contanstant.CollectionType;
import org.beifengtz.jvmm.core.entity.JvmmData;
import org.beifengtz.jvmm.core.entity.info.PortInfo;
import org.beifengtz.jvmm.core.entity.info.ThreadPoolInfo;
import org.beifengtz.jvmm.server.entity.conf.ThreadPoolConf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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

    /**
     * 根据采集项收集数据
     *
     * @param options             {@link CollectionType}采集项
     * @param listenedPorts       如果 options 包含{@link CollectionType#port}，此参数表示需要监听的端口列表
     * @param listenedThreadPools 如果 options 包含了 {@link CollectionType#jvm_thread_pool}，此参数表示需要监听的线程池信息
     * @param consumer            异步回调，采集结束后回调返回{@link JvmmData}，其中有 left 和 right 两个值：
     *                            left  - 为当前还需要等待的异步项数量，如果小于等于0则表示已采集完；
     *                            right - 为当前采集的数据，当采集完时该数据才是有效数据。
     */
    static void collectByOptions(List<CollectionType> options,
                                 List<Integer> listenedPorts,
                                 List<ThreadPoolConf> listenedThreadPools,
                                 Consumer<PairKey<AtomicInteger, JvmmData>> consumer) {
        JvmmData res = new JvmmData();
        JvmmCollector collector = JvmmFactory.getCollector();
        AtomicInteger asyncNum = new AtomicInteger(0);
        for (CollectionType type : options) {
            if (type == null) {
                continue;
            }
            switch (type) {
                case process:
                    res.setProcess(collector.getProcess());
                    break;
                case disk:
                    res.setDisk(collector.getDisk());
                    break;
                case disk_io:
                    res.setDiskIO(collector.getDiskIO());
                    break;
                case cpu:
                    res.setCpu(collector.getCPUInfo());
                    break;
                case network:
                    asyncNum.incrementAndGet();
                    collector.getNetwork().thenAccept(info -> {
                        res.setNetwork(info);
                        asyncNum.decrementAndGet();
                        consumer.accept(PairKey.of(asyncNum, res));
                    });
                    break;
                case sys:
                    res.setSys(collector.getSys());
                    break;
                case sys_memory:
                    res.setSysMem(collector.getSysMem());
                    break;
                case sys_file:
                    res.setSysFile(collector.getSysFile());
                    break;
                case port:
                    PortInfo portInfo = new PortInfo();
                    if (listenedPorts != null && !listenedPorts.isEmpty()) {
                        res.setPort(collector.getPortInfo(listenedPorts));
                    }
                    res.setPort(portInfo);
                    break;
                case jvm_classloading:
                    res.setJvmClassLoading(collector.getJvmClassLoading());
                    break;
                case jvm_classloader:
                    res.setJvmClassLoader(collector.getJvmClassLoaders());
                    break;
                case jvm_compilation:
                    res.setJvmCompilation(collector.getJvmCompilation());
                    break;
                case jvm_gc:
                    res.setJvmGc(collector.getJvmGC());
                    break;
                case jvm_memory:
                    res.setJvmMemory(collector.getJvmMemory());
                    break;
                case jvm_memory_manager:
                    res.setJvmMemoryManager(collector.getJvmMemoryManager());
                    break;
                case jvm_memory_pool:
                    res.setJvmMemoryPool(collector.getJvmMemoryPool());
                    break;
                case jvm_thread:
                    res.setJvmThread(collector.getJvmThread());
                    break;
                case jvm_thread_stack:
                    res.setJvmStack(collector.dumpAllThreads());
                    break;
                case jvm_thread_detail:
                    res.setJvmThreadDetail(collector.getAllJvmThreadDetailInfo());
                    break;
                case jvm_thread_pool:
                    if (listenedThreadPools != null && !listenedThreadPools.isEmpty()) {
                        List<ThreadPoolInfo> infos = new ArrayList<>(listenedThreadPools.size());
                        for (ThreadPoolConf tp : listenedThreadPools) {
                            ThreadPoolInfo info;
                            if (tp.getInstanceFiled() == null) {
                                info = collector.getThreadPoolInfo(tp.getClassPath(), tp.getFiled());
                            } else {
                                info = collector.getThreadPoolInfo(tp.getClassPath(), tp.getInstanceFiled(), tp.getFiled());
                            }
                            info.setName(tp.getName());
                            infos.add(info);
                        }
                        res.setThreadPool(infos);
                    }
                    break;
            }
        }
        if (asyncNum.get() <= 0) {
            consumer.accept(PairKey.of(asyncNum, res));
        }
    }
}
