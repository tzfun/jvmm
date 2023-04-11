package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * description: TODO
 * date 14:36 2023/1/31
 * @author beifengtz
 */
public class CPUInfo implements JsonParsable {

    /**
     * CPU核心数
     */
    private int cpuNum;
    /**
     * 系统使用率
     */
    private double sys;
    /**
     * 用户使用率
     */
    private double user;
    /**
     * IO等待
     */
    private double ioWait;
    /**
     * 空闲率
     */
    private double idle;

    private CPUInfo() {
    }

    public static CPUInfo create() {
        return new CPUInfo();
    }

    @Override
    public String toString() {
        return toJsonStr();
    }

    public int getCpuNum() {
        return cpuNum;
    }

    public CPUInfo setCpuNum(int cpuNum) {
        this.cpuNum = cpuNum;
        return this;
    }

    public double getSys() {
        return sys;
    }

    public CPUInfo setSys(double sys) {
        this.sys = sys;
        return this;
    }

    public double getUser() {
        return user;
    }

    public CPUInfo setUser(double user) {
        this.user = user;
        return this;
    }

    public double getIoWait() {
        return ioWait;
    }

    public CPUInfo setIoWait(double ioWait) {
        this.ioWait = ioWait;
        return this;
    }

    public double getIdle() {
        return idle;
    }

    public CPUInfo setIdle(double idle) {
        this.idle = idle;
        return this;
    }
}
