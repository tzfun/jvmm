package org.beifengtz.jvmm.server.entity.conf;

import org.beifengtz.jvmm.core.contanstant.CollectionType;
import org.beifengtz.jvmm.core.entity.conf.ThreadPoolConf;

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
    private List<SentinelSubscriberConf> subscribers = new ArrayList<>();
    /**
     * 采集项
     */
    private List<CollectionType> tasks = new ArrayList<>();
    /**
     * 采集周期，秒
     */
    private int interval = 10;
    private List<Integer> listenedPorts;
    private List<ThreadPoolConf> listenedThreadPools;

    public List<SentinelSubscriberConf> getSubscribers() {
        return subscribers;
    }

    public SentinelConf setSubscribers(List<SentinelSubscriberConf> subscribers) {
        this.subscribers = subscribers;
        return this;
    }

    public SentinelConf addSubscriber(SentinelSubscriberConf subscriber) {
        this.subscribers.add(subscriber);
        return this;
    }

    public SentinelConf clearSubscriber() {
        this.subscribers.clear();
        return this;
    }

    public List<CollectionType> getTasks() {
        return tasks;
    }

    public SentinelConf setTasks(List<CollectionType> tasks) {
        this.tasks = tasks;
        return this;
    }

    public SentinelConf addTasks(CollectionType task) {
        this.tasks.add(task);
        return this;
    }

    public SentinelConf clearTasks() {
        this.tasks.clear();
        return this;
    }

    public int getInterval() {
        return Math.max(1, interval);
    }

    public SentinelConf setInterval(int interval) {
        this.interval = interval;
        return this;
    }

    public List<Integer> getListenedPorts() {
        return listenedPorts;
    }

    public SentinelConf setListenedPorts(List<Integer> listenedPorts) {
        this.listenedPorts = listenedPorts;
        return this;
    }

    public List<ThreadPoolConf> getListenedThreadPools() {
        return listenedThreadPools;
    }

    public SentinelConf setListenedThreadPools(List<ThreadPoolConf> listenedThreadPools) {
        this.listenedThreadPools = listenedThreadPools;
        return this;
    }
}
