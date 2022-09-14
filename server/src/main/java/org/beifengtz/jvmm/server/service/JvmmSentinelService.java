package org.beifengtz.jvmm.server.service;

import io.netty.util.concurrent.Promise;
import org.beifengtz.jvmm.common.factory.ExecutorFactory;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.util.CommonUtil;
import org.beifengtz.jvmm.common.util.HttpUtil;
import org.beifengtz.jvmm.core.JvmmCollector;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.server.entity.conf.AuthOptionConf;
import org.beifengtz.jvmm.server.entity.conf.SentinelConf;
import org.beifengtz.jvmm.server.entity.conf.SubscriberConf;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.entity.dto.JvmmDataDTO;
import org.slf4j.Logger;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
    protected AtomicBoolean flag = new AtomicBoolean(true);
    protected AtomicInteger counter = new AtomicInteger(0);

    /**
     * 连续失败次数，如果超过10次后按 2的n次方 次数重试
     */
    protected final Map<String, AtomicInteger> failTimes = new HashMap<>();
    protected final Map<String, AtomicInteger> failStepCounter = new HashMap<>();

    public JvmmSentinelService() {
        this(ExecutorFactory.getScheduleThreadPool());
    }

    public JvmmSentinelService(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void start(Promise<Integer> promise) {
        if(ServerContext.getConfiguration().getServer().getSentinel().isValid()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    SentinelConf conf = ServerContext.getConfiguration().getServer().getSentinel();
                    try {
                        int count = counter.incrementAndGet();
                        JvmmDataDTO data = JvmmService.collectByOptions(conf.getOptions());
                        data.setNode(ServerContext.getConfiguration().getName());

                        if (count <= conf.getSendStaticInfoTimes()) {
                            JvmmCollector collector = JvmmFactory.getCollector();
                            if (data.getProcess() == null) {
                                data.setProcess(collector.getProcess());
                            }
                            if (data.getSystem() == null) {
                                data.setSystem(collector.getSystemStatic());
                            }
                        }
                        String body = data.toJsonStr();
                        for (SubscriberConf subscriber : conf.getSubscribers()) {
                            publish(subscriber, body);
                        }
                    } catch (Exception e) {
                        logger.error("Jvmm sentinel error: " + e.getMessage(), e);
                    } finally {
                        if (flag.get()) {
                            executor.schedule(this, conf.getInterval(), TimeUnit.SECONDS);
                        } else {
                            logger.info("Jvmm sentinel shutdown by trigger");
                        }
                    }
                }
            });
            promise.trySuccess(0);
        } else {
            promise.tryFailure(new RuntimeException("Jvmm sentinel is invalid, no subscribers."));
        }

    }

    @Override
    public void stop() {
        logger.info("Trigger to shutdown jvmm sentinel service...");
        flag.set(false);
    }

    protected void publish(SubscriberConf subscriber, String body) {
        AtomicInteger failCounter = failTimes.get(subscriber.getUrl());
        try {
            if (failCounter.get() >= 10) {
                int step = failStepCounter.get(subscriber.getUrl()).incrementAndGet();
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

            HttpUtil.post(subscriber.getUrl(), body, headers);
            failCounter.set(0);
            failStepCounter.get(subscriber.getUrl()).set(0);
        } catch (ConnectException | SocketTimeoutException e) {
            logger.debug("Can not connect monitor subscriber '{}': {}", subscriber.getUrl(), e.getMessage());
            failCounter.incrementAndGet();
        } catch (Exception e) {
            logger.error("Monitor publish to " + subscriber.getUrl() + " failed: " + e.getMessage(), e);
            failCounter.incrementAndGet();
        }
    }

    /**
     * 判断一个数是否是 2的n次方
     */
    protected boolean judge(int n) {
        return (n & (n - 1)) == 0;
    }
}
