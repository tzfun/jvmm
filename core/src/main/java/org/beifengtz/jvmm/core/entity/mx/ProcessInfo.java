package org.beifengtz.jvmm.core.entity.mx;

import org.beifengtz.jvmm.core.entity.JsonParsable;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: TODO 进程信息
 * </p>
 * <p>
 * Created in 14:53 2021/5/11
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

    /**
     * 进程环境及启动信息
     */
    private boolean bootClassPathSupported;
    private String bootClassPath;
    private String classPath;
    private String libraryPath;
    private List<String> inputArgs;
    private Map<String, String> systemProperties;

    /**
     * 虚拟机规范信息
     */
    private String managementSpecVersion;
    private String specName;
    private String specVendor;
    private String specVersion;

    private ProcessInfo(){}

    public static ProcessInfo create(){
        return new ProcessInfo();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public String getVmVersion() {
        return vmVersion;
    }

    public void setVmVersion(String vmVersion) {
        this.vmVersion = vmVersion;
    }

    public String getVmVendor() {
        return vmVendor;
    }

    public void setVmVendor(String vmVendor) {
        this.vmVendor = vmVendor;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public boolean isBootClassPathSupported() {
        return bootClassPathSupported;
    }

    public void setBootClassPathSupported(boolean bootClassPathSupported) {
        this.bootClassPathSupported = bootClassPathSupported;
    }

    public String getBootClassPath() {
        return bootClassPath;
    }

    public void setBootClassPath(String bootClassPath) {
        this.bootClassPath = bootClassPath;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getLibraryPath() {
        return libraryPath;
    }

    public void setLibraryPath(String libraryPath) {
        this.libraryPath = libraryPath;
    }

    public List<String> getInputArgs() {
        return inputArgs;
    }

    public void setInputArgs(List<String> inputArgs) {
        this.inputArgs = inputArgs;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }

    public String getManagementSpecVersion() {
        return managementSpecVersion;
    }

    public void setManagementSpecVersion(String managementSpecVersion) {
        this.managementSpecVersion = managementSpecVersion;
    }

    public String getSpecName() {
        return specName;
    }

    public void setSpecName(String specName) {
        this.specName = specName;
    }

    public String getSpecVendor() {
        return specVendor;
    }

    public void setSpecVendor(String specVendor) {
        this.specVendor = specVendor;
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
