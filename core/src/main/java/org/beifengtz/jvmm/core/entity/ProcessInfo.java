package org.beifengtz.jvmm.core.entity;

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
public class ProcessInfo {
    /**
     * 进程信息
     */
    private String name;
    private long startTime;
    private long uptime;
    private int pid;

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
}
