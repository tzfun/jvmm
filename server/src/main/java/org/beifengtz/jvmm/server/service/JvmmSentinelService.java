package org.beifengtz.jvmm.server.service;

import io.netty.util.concurrent.Promise;
import org.beifengtz.jvmm.common.factory.ExecutorFactory;
import org.beifengtz.jvmm.common.util.CommonUtil;
import org.beifengtz.jvmm.common.util.HttpUtil;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.entity.conf.AuthOptionConf;
import org.beifengtz.jvmm.server.entity.conf.SentinelConf;
import org.beifengtz.jvmm.server.entity.conf.SentinelSubscriberConf;
import org.beifengtz.jvmm.server.entity.dto.JvmmDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 09:50 2022/9/7
 *
 * @author beifengtz
 */
public class JvmmSentinelService implements JvmmService {

    private static final Logger logger = LoggerFactory.getLogger(JvmmHttpServerService.class);
    protected static final Map<String, String> globalHeaders = CommonUtil.hasMapOf("Content-Type", "application/json;charset=UTF-8");

    protected ScheduledExecutorService executor;
    protected ScheduledFuture<?> scheduledFuture;

    /**
     * 连续失败次数，如果超过10次后按 interval的n倍 次重试
     */
    protected final Map<String, AtomicInteger> failTimes = new ConcurrentHashMap<>();
    protected final Map<String, AtomicInteger> failStepCounter = new ConcurrentHashMap<>();
    protected Set<ShutdownListener> shutdownListeners = new HashSet<>();

    protected Queue<SentinelTask> taskList = new ConcurrentLinkedQueue<>();

    public JvmmSentinelService() {
        this(ExecutorFactory.getScheduleThreadPool());
    }

    public JvmmSentinelService(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void start(Promise<Integer> promise) {
        List<SentinelConf> sentinels = ServerContext.getConfiguration().getServer().getSentinel();
        sentinels = sentinels.stream().filter(o -> o.getSubscribers().size() > 0 && o.getTasks().size() > 0).collect(Collectors.toList());
        if (sentinels.size() > 0) {

            //  初始化任务
            taskList.clear();

            long now = System.currentTimeMillis();
            int minInterval = Integer.MAX_VALUE;
            for (SentinelConf conf : sentinels) {
                SentinelTask task = new SentinelTask();
                task.conf = conf;
                task.execTime = now + conf.getInterval() * 1000L;
                minInterval = Math.min(minInterval, conf.getInterval());
                taskList.add(task);
            }

            scheduledFuture = executor.scheduleWithFixedDelay(() -> {
                Iterator<SentinelTask> it = taskList.iterator();
                while (it.hasNext()) {
                    SentinelTask task = it.next();
                    if (System.currentTimeMillis() >= task.execTime) {
                        if (task.run()) {
                            it.remove();
                        }
                    }
                }
            }, 0, minInterval, TimeUnit.SECONDS);

            promise.trySuccess(0);
            logger.info("Jvmm sentinel service started, sentinel num:{}, min interval: {}", sentinels.size(), minInterval);
        } else {
            promise.tryFailure(new RuntimeException("No valid jvmm sentinel configuration."));
        }
    }

    @Override
    public void shutdown() {
        logger.info("Trigger to shutdown jvmm sentinel service...");
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        for (ShutdownListener listener : shutdownListeners) {
            try {
                listener.onShutdown();
            } catch (Exception e) {
                logger.error("An exception occurred while executing the shutdown listener: " + e.getMessage(), e);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JvmmService> T addShutdownListener(ShutdownListener listener) {
        shutdownListeners.add(listener);
        return (T) this;
    }

    protected void publish(SentinelSubscriberConf subscriber, String body, int interval) {
        String url = subscriber.getUrl();
        AtomicInteger failCounter = failTimes.computeIfAbsent(url, o -> new AtomicInteger(0));
        failStepCounter.computeIfAbsent(url, o -> new AtomicInteger(0));
        try {
            if (failCounter.get() >= 10) {
                int step = failStepCounter.get(url).incrementAndGet();
                if (step % interval != 0) {
                    return;
                }
            }
            Map<String, String> headers;
            AuthOptionConf auth = subscriber.getAuth();
            if (auth != null && auth.isEnable()) {
                headers = new HashMap<>(globalHeaders);
                String authKey = Base64.getEncoder().encodeToString((auth.getUsername() + ":" + auth.getPassword()).getBytes(StandardCharsets.UTF_8));
                headers.put("Authorization", "Basic " + authKey);
            } else {
                headers = globalHeaders;
            }

            HttpUtil.post(url, body, headers);
            failCounter.set(0);
            failStepCounter.get(url).set(0);
        } catch (IOException e) {
            logger.warn("Can not connect monitor subscriber '{}': {}", url, e.getMessage());
            failCounter.incrementAndGet();
        } catch (Exception e) {
            logger.error("Monitor publish to " + url + " failed: " + e.getMessage(), e);
            failCounter.incrementAndGet();
        }
    }

    @Override
    public int hashCode() {
        return 3;
    }

    class SentinelTask {
        public SentinelConf conf;
        public long execTime;
        public int counter = 0;

        /**
         * 执行任务
         *
         * @return true-任务已结束，需要从任务队列中移除；false-还有下一次任务
         */
        public boolean run() {
            counter++;
            if (conf.getCount() > 0 && counter > conf.getCount()) {
                return true;
            }
            executor.execute(() -> JvmmService.collectByOptions(conf.getTasks(), pair -> {
                if (pair.getLeft().get() <= 0) {
                    JvmmDataDTO data = pair.getRight().setNode(ServerContext.getConfiguration().getName());
                    String body = data.toJsonStr();
                    for (SentinelSubscriberConf subscriber : conf.getSubscribers()) {
                        executor.submit(() -> publish(subscriber, body, conf.getInterval()));
                    }
                    execTime = System.currentTimeMillis() + conf.getInterval() * 1000L;
                }
            }));
            return false;
        }
    }
}
