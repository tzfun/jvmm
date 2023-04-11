package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

import java.util.List;

/**
 * description: 进程信息
 * date 14:53 2021/5/11
 *
 * @author beifengtz
 */
public class ProcessInfo implements JsonParsable {
    /**
     * 进程信息
     */
    private String name;
    private long startTime;
    private long uptime;
    private long pid;

    /**
     * JVM信息
     */
    private String vmVersion;
    private String vmVendor;
    private String vmName;
    private String vmHome;

    /**
     * 虚拟机规范信息
     */
    private String vmManagementSpecVersion;
    private String vmSpecName;
    private String vmSpecVendor;
    private String vmSpecVersion;

    /**
     * 进程环境及启动信息
     */
    private List<String> inputArgs;
    private String workDir;

    private ProcessInfo() {
    }

    public static ProcessInfo create() {
        return new ProcessInfo();
    }

    public String getName() {
        return name;
    }

    public ProcessInfo setName(String name) {
        this.name = name;
        return this;
    }

    public long getStartTime() {
        return startTime;
    }

    public ProcessInfo setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public long getUptime() {
        return uptime;
    }

    public ProcessInfo setUptime(long uptime) {
        this.uptime = uptime;
        return this;
    }

    public long getPid() {
        return pid;
    }

    public ProcessInfo setPid(long pid) {
        this.pid = pid;
        return this;
    }

    public String getVmVersion() {
        return vmVersion;
    }

    public ProcessInfo setVmVersion(String vmVersion) {
        this.vmVersion = vmVersion;
        return this;
    }

    public String getVmVendor() {
        return vmVendor;
    }

    public ProcessInfo setVmVendor(String vmVendor) {
        this.vmVendor = vmVendor;
        return this;
    }

    public String getVmName() {
        return vmName;
    }

    public ProcessInfo setVmName(String vmName) {
        this.vmName = vmName;
        return this;
    }

    public String getVmHome() {
        return vmHome;
    }

    public ProcessInfo setVmHome(String vmHome) {
        this.vmHome = vmHome;
        return this;
    }

    public List<String> getInputArgs() {
        return inputArgs;
    }

    public ProcessInfo setInputArgs(List<String> inputArgs) {
        this.inputArgs = inputArgs;
        return this;
    }

    public String getWorkDir() {
        return workDir;
    }

    public ProcessInfo setWorkDir(String workDir) {
        this.workDir = workDir;
        return this;
    }

    public String getVmManagementSpecVersion() {
        return vmManagementSpecVersion;
    }

    public ProcessInfo setVmManagementSpecVersion(String vmManagementSpecVersion) {
        this.vmManagementSpecVersion = vmManagementSpecVersion;
        return this;
    }

    public String getVmSpecName() {
        return vmSpecName;
    }

    public ProcessInfo setVmSpecName(String vmSpecName) {
        this.vmSpecName = vmSpecName;
        return this;
    }

    public String getVmSpecVendor() {
        return vmSpecVendor;
    }

    public ProcessInfo setVmSpecVendor(String vmSpecVendor) {
        this.vmSpecVendor = vmSpecVendor;
        return this;
    }

    public String getVmSpecVersion() {
        return vmSpecVersion;
    }

    public ProcessInfo setVmSpecVersion(String vmSpecVersion) {
        this.vmSpecVersion = vmSpecVersion;
        return this;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
