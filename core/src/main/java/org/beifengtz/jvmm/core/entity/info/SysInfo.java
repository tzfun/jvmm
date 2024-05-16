package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * description: 宿主机信息
 * date 15:05 2021/5/11
 * @author beifengtz
 */
public class SysInfo implements JsonParsable {
    /**
     * 系统名
     */
    private String name;
    /**
     * 系统版本
     */
    private String version;
    /**
     * 体系架构
     */
    private String arch;
    /**
     * 可用处理器数量
     */
    private int cpuNum;
    /**
     * 使用时区
     */
    private String timeZone;
    /**
     * IP
     */
    private String ip;
    /**
     * 用户
     */
    private String user;
    /**
     * 平均负载
     */
    private double loadAverage;
    /**
     * 系统自启动到现在的时间，单位秒
     */
    private long uptime;
    /**
     * 系统启动时间，时间戳，单位秒
     */
    private long bootTime;
    /**
     * 操作系统位数
     */
    private int bitness;
    /**
     * 线程数
     */
    private int threadCount;
    /**
     * 进程数
     */
    private int processCount;


    private SysInfo(){}

    public static SysInfo create(){
        return new SysInfo();
    }

    public String getName() {
        return name;
    }

    public SysInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public SysInfo setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getArch() {
        return arch;
    }

    public SysInfo setArch(String arch) {
        this.arch = arch;
        return this;
    }

    public int getCpuNum() {
        return cpuNum;
    }

    public SysInfo setCpuNum(int cpuNum) {
        this.cpuNum = cpuNum;
        return this;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public SysInfo setTimeZone(String timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public SysInfo setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getUser() {
        return user;
    }

    public SysInfo setUser(String user) {
        this.user = user;
        return this;
    }

    public double getLoadAverage() {
        return loadAverage;
    }

    public SysInfo setLoadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
        return this;
    }

    public long getUptime() {
        return uptime;
    }

    public SysInfo setUptime(long uptime) {
        this.uptime = uptime;
        return this;
    }

    public long getBootTime() {
        return bootTime;
    }

    public SysInfo setBootTime(long bootTime) {
        this.bootTime = bootTime;
        return this;
    }

    public int getBitness() {
        return bitness;
    }

    public SysInfo setBitness(int bitness) {
        this.bitness = bitness;
        return this;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public SysInfo setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    public int getProcessCount() {
        return processCount;
    }

    public SysInfo setProcessCount(int processCount) {
        this.processCount = processCount;
        return this;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
