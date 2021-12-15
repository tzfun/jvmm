package org.beifengtz.jvmm.core.conf;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:06 2021/5/22
 *
 * @author beifengtz
 */
public class ConfigFileMapping {
    private String name;
    private String workThread;
    private String fromAgent;
    private Map<String, String> port = new HashMap<>();
    private Map<String, String> http = new HashMap<>();
    private Map<String, String> security = new HashMap<>();
    private Map<String, String> log = new HashMap<>();

    public String getName() {
        return name;
    }

    public Map<String, String> getPort() {
        return port;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(Map<String, String> port) {
        this.port = port;
    }

    public Map<String, String> getHttp() {
        return http;
    }

    public void setHttp(Map<String, String> http) {
        this.http = http;
    }

    public Map<String, String> getSecurity() {
        return security;
    }

    public void setSecurity(Map<String, String> security) {
        this.security = security;
    }

    public Map<String, String> getLog() {
        return log;
    }

    public void setLog(Map<String, String> log) {
        this.log = log;
    }

    public String getWorkThread() {
        return workThread;
    }

    public void setWorkThread(String workThread) {
        this.workThread = workThread;
    }

    public String getFromAgent() {
        return fromAgent;
    }

    public void setFromAgent(String fromAgent) {
        this.fromAgent = fromAgent;
    }
}
