package org.beifengtz.jvmm.core.entity;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:11 2021/5/11
 *
 * @author beifengtz
 */
public class ThreadDynamicInfo implements JsonParsable {
    private long[] deadlockedThreads;
    /**
     * 启动Java虚拟机或重置峰值以来的活动线程峰值
     */
    private int peakThreadCount;
    /**
     * 当前活跃的守护线程数
     */
    private int daemonThreadCount;
    /**
     * 活动线程的当前数量，包括守护程序线程和非守护程序线程
     */
    private int threadCount;
    /**
     * 自Java虚拟机启动以来创建和启动的线程总数
     */
    private long totalStartedThreadCount;

    private ThreadDynamicInfo(){
    }

    public static ThreadDynamicInfo create(){
        return new ThreadDynamicInfo();
    }

    public long[] getDeadlockedThreads() {
        return deadlockedThreads;
    }

    public void setDeadlockedThreads(long[] deadlockedThreads) {
        this.deadlockedThreads = deadlockedThreads;
    }

    public int getPeakThreadCount() {
        return peakThreadCount;
    }

    public void setPeakThreadCount(int peakThreadCount) {
        this.peakThreadCount = peakThreadCount;
    }

    public int getDaemonThreadCount() {
        return daemonThreadCount;
    }

    public void setDaemonThreadCount(int daemonThreadCount) {
        this.daemonThreadCount = daemonThreadCount;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public long getTotalStartedThreadCount() {
        return totalStartedThreadCount;
    }

    public void setTotalStartedThreadCount(long totalStartedThreadCount) {
        this.totalStartedThreadCount = totalStartedThreadCount;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
