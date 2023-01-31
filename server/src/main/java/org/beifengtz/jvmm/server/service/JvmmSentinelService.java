package org.beifengtz.jvmm.server.service;

import io.netty.util.concurrent.Promise;
import org.beifengtz.jvmm.common.factory.ExecutorFactory;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.util.CommonUtil;
import org.beifengtz.jvmm.common.util.HttpUtil;
import org.beifengtz.jvmm.core.JvmmCollector;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.entity.conf.AuthOptionConf;
import org.beifengtz.jvmm.server.entity.conf.SentinelConf;
import org.beifengtz.jvmm.server.entity.conf.SubscriberConf;
import org.beifengtz.jvmm.server.entity.dto.JvmmDataDTO;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    private static final Logger logger = LoggerFactory.logger(JvmmHttpServerService.class);
    protected static final Map<String, String> globalHeaders = CommonUtil.hasMapOf("Content-Type", "application/json;charset=UTF-8");

    protected ScheduledExecutorService executor;
    protected ScheduledFuture<?> scheduledFuture;
    protected AtomicInteger counter = new AtomicInteger(0);

    /**
     * 连续失败次数，如果超过10次后按 2的n次方 次数重试
     */
    protected final Map<String, AtomicInteger> failTimes = new ConcurrentHashMap<>();
    protected final Map<String, AtomicInteger> failStepCounter = new ConcurrentHashMap<>();
    protected Set<ShutdownListener> shutdownListeners = new HashSet<>();

    public JvmmSentinelService() {
        this(ExecutorFactory.getScheduleThreadPool());
    }

    public JvmmSentinelService(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void start(Promise<Integer> promise) {
        if (ServerContext.getConfiguration().getServer().getSentinel().isValid()) {
            SentinelConf conf = ServerContext.getConfiguration().getServer().getSentinel();
            scheduledFuture = executor.scheduleWithFixedDelay(() -> {
                int count = counter.incrementAndGet();
                JvmmDataDTO data = JvmmService.collectByOptions(conf.getOptions());
                data.setNode(ServerContext.getConfiguration().getName());

                if (count <= conf.getSendStaticInfoTimes()) {
                    JvmmCollector collector = JvmmFactory.getCollector();
                    if (data.getProcess() == null) {
                        data.setProcess(collector.getProcess());
                    }
                    if (data.getSystem() == null) {
                        data.setSystem(collector.getSys());
                    }
                }
                String body = data.toJsonStr();
                for (SubscriberConf subscriber : conf.getSubscribers()) {
                    publish(subscriber, body);
                }
            }, 0, conf.getInterval(), TimeUnit.SECONDS);

            promise.trySuccess(0);
            logger.info("Jvmm sentinel service started, subscriber num:{}, interval: {}s", conf.getSubscribers().size(), conf.getInterval());
        } else {
            promise.tryFailure(new RuntimeException("Jvmm sentinel is invalid, no subscribers."));
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

    protected void publish(SubscriberConf subscriber, String body) {
        String url = subscriber.getUrl();
        AtomicInteger failCounter = failTimes.computeIfAbsent(url, o -> new AtomicInteger(0));
        failStepCounter.computeIfAbsent(url, o -> new AtomicInteger(0));
        try {
            if (failCounter.get() >= 10) {
                int step = failStepCounter.get(url).incrementAndGet();
                if (!judge(step)) {
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

    /**
     * 判断一个数是否是 2的n次方
     * @param n value
     * @return true-判定通过
     */
    protected boolean judge(int n) {
        return (n & (n - 1)) == 0;
    }

    @Override
    public int hashCode() {
        return 3;
    }
}
