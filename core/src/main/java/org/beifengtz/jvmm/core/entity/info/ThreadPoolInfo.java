package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * description: 线程池采集信息
 * date: 10:11 2023/4/25
 *
 * @author beifengtz
 */
public class ThreadPoolInfo implements JsonParsable {
    private String name;
    //  配置信息
    private String threadFactory;
    private String rejectHandler;
    private int corePoolSize;
    private int maximumPoolSize;
    private long keepAliveMillis;
    private String queue;

    //  下面是动态信息
    private String state;
    private boolean allowsCoreThreadTimeOut;
    private int queueSize;
    /**
     * 当前线程数
     */
    private int threadCount;
    /**
     * 当前正在执行任务的线程数
     */
    private int activeThreadCount;
    private int largestThreadCount;
    private long taskCount;
    private long completedTaskCount;

    private ThreadPoolInfo() {

    }

    public static ThreadPoolInfo create() {
        return new ThreadPoolInfo();
    }

    public String getName() {
        return name;
    }

    public ThreadPoolInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getState() {
        return state;
    }

    public ThreadPoolInfo setState(String state) {
        this.state = state;
        return this;
    }

    public String getThreadFactory() {
        return threadFactory;
    }

    public ThreadPoolInfo setThreadFactory(String threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public String getRejectHandler() {
        return rejectHandler;
    }

    public ThreadPoolInfo setRejectHandler(String rejectHandler) {
        this.rejectHandler = rejectHandler;
        return this;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public ThreadPoolInfo setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    public boolean isAllowsCoreThreadTimeOut() {
        return allowsCoreThreadTimeOut;
    }

    public ThreadPoolInfo setAllowsCoreThreadTimeOut(boolean allowsCoreThreadTimeOut) {
        this.allowsCoreThreadTimeOut = allowsCoreThreadTimeOut;
        return this;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public ThreadPoolInfo setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
        return this;
    }

    public long getKeepAliveMillis() {
        return keepAliveMillis;
    }

    public ThreadPoolInfo setKeepAliveMillis(long keepAliveMillis) {
        this.keepAliveMillis = keepAliveMillis;
        return this;
    }

    public String getQueue() {
        return queue;
    }

    public ThreadPoolInfo setQueue(String queue) {
        this.queue = queue;
        return this;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public ThreadPoolInfo setQueueSize(int queueSize) {
        this.queueSize = queueSize;
        return this;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public ThreadPoolInfo setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    public int getActiveThreadCount() {
        return activeThreadCount;
    }

    public ThreadPoolInfo setActiveThreadCount(int activeThreadCount) {
        this.activeThreadCount = activeThreadCount;
        return this;
    }

    public int getLargestThreadCount() {
        return largestThreadCount;
    }

    public ThreadPoolInfo setLargestThreadCount(int largestThreadCount) {
        this.largestThreadCount = largestThreadCount;
        return this;
    }

    public long getTaskCount() {
        return taskCount;
    }

    public ThreadPoolInfo setTaskCount(long taskCount) {
        this.taskCount = taskCount;
        return this;
    }

    public long getCompletedTaskCount() {
        return completedTaskCount;
    }

    public ThreadPoolInfo setCompletedTaskCount(long completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
        return this;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
