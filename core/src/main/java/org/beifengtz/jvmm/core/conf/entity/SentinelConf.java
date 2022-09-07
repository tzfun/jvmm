package org.beifengtz.jvmm.core.conf.entity;

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
    private List<SubscriberConf> subscribers;
    private int interval;
    private int senStaticInfoTimes;
    private SentinelOptions options;

    public List<SubscriberConf> getSubscribers() {
        return subscribers;
    }

    public SentinelConf setSubscribers(List<SubscriberConf> subscribers) {
        this.subscribers = subscribers;
        return this;
    }

    public int getInterval() {
        return interval;
    }

    public SentinelConf setInterval(int interval) {
        this.interval = interval;
        return this;
    }

    public int getSenStaticInfoTimes() {
        return senStaticInfoTimes;
    }

    public SentinelConf setSenStaticInfoTimes(int senStaticInfoTimes) {
        this.senStaticInfoTimes = senStaticInfoTimes;
        return this;
    }

    public SentinelOptions getOptions() {
        return options;
    }

    public SentinelConf setOptions(SentinelOptions options) {
        this.options = options;
        return this;
    }
}
