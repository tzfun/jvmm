package org.beifengtz.jvmm.server.entity.conf;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:32 2022/9/7
 *
 * @author beifengtz
 */
public class SentinelConf {
    private List<SubscriberConf> subscribers = new ArrayList<>();
    private int interval;
    private int sendStaticInfoTimes;
    private CollectOptions options;

    public List<SubscriberConf> getSubscribers() {
        return subscribers;
    }

    public SentinelConf setSubscribers(List<SubscriberConf> subscribers) {
        this.subscribers = subscribers;
        return this;
    }

    public SentinelConf addSubscribers(SubscriberConf subscriber) {
        this.subscribers.add(subscriber);
        return this;
    }

    public int getInterval() {
        return interval;
    }

    public SentinelConf setInterval(int interval) {
        this.interval = interval;
        return this;
    }

    public int getSendStaticInfoTimes() {
        return sendStaticInfoTimes;
    }

    public SentinelConf setSendStaticInfoTimes(int sendStaticInfoTimes) {
        this.sendStaticInfoTimes = sendStaticInfoTimes;
        return this;
    }

    public CollectOptions getOptions() {
        return options;
    }

    public SentinelConf setOptions(CollectOptions options) {
        this.options = options;
        return this;
    }

    public boolean isValid() {
        return subscribers != null && subscribers.size() > 0;
    }
}
