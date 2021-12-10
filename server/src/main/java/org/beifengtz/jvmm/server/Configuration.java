package org.beifengtz.jvmm.server;

import com.google.gson.Gson;
import org.beifengtz.jvmm.tools.util.StringUtil;

import java.util.Map;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:48 2021/5/22
 *
 * @author beifengtz
 */
public final class Configuration {

    private final String name;
    private final int port;
    private final boolean autoIncrease;
    private final int httpMaxChunkSize;

    private final boolean securityEnable;
    private final String securityAccount;
    private final String securityPassword;

    private final String logLevel;

    private final int workThread;

    private Configuration(Builder builder) {
        this.name = builder.name;
        this.port = builder.port;
        this.autoIncrease = builder.autoIncrease;
        this.httpMaxChunkSize = builder.httpMaxChunkSize;
        this.securityEnable = builder.securityEnable;
        this.securityAccount = builder.securityAccount;
        this.securityPassword = builder.securityPassword;
        this.logLevel = builder.logLevel;
        this.workThread = builder.workThread;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Configuration defaultInstance() {
        return new Builder().build();
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public String argFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=").append(name).append(";");
        sb.append("port.bind=").append(port).append(";");
        sb.append("port.autoIncrease=").append(autoIncrease).append(";");
        sb.append("http.maxChunkSize=").append(httpMaxChunkSize).append(";");

        sb.append("security.account=").append(securityAccount).append(";");
        sb.append("security.password=").append(securityPassword).append(";");
        sb.append("security.enable=").append(securityEnable).append(";");

        sb.append("log.level=").append(logLevel).append(";");
        sb.append("workThread=").append(workThread).append(";");
        return sb.toString();
    }

    static class Builder {
        private String name = "jvmm_server";
        private int port = 5010;
        private boolean autoIncrease = true;
        private int httpMaxChunkSize = 52428800;   //  50MB
        private boolean securityEnable = false;
        private String securityAccount = "";
        private String securityPassword = "";

        private String logLevel = "info";

        private int workThread = 1;

        public Configuration build() {
            return new Configuration(this);
        }

        public int getPort() {
            return port;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public boolean isAutoIncrease() {
            return autoIncrease;
        }

        public Builder setAutoIncrease(boolean autoIncrease) {
            this.autoIncrease = autoIncrease;
            return this;
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public int getHttpMaxChunkSize() {
            return httpMaxChunkSize;
        }

        public Builder setHttpMaxChunkSize(int httpMaxChunkSize) {
            this.httpMaxChunkSize = httpMaxChunkSize;
            return this;
        }

        public boolean isSecurityEnable() {
            return securityEnable;
        }

        public Builder setSecurityEnable(boolean securityEnable) {
            this.securityEnable = securityEnable;
            return this;
        }

        public String getSecurityAccount() {
            return securityAccount;
        }

        public Builder setSecurityAccount(String securityAccount) {
            this.securityAccount = securityAccount;
            return this;
        }

        public String getSecurityPassword() {
            return securityPassword;
        }

        public Builder setSecurityPassword(String securityPassword) {
            this.securityPassword = securityPassword;
            return this;
        }

        public String getLogLevel() {
            return logLevel;
        }

        public Builder setLogLevel(String logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public int getWorkThread() {
            return workThread;
        }

        public Builder setWorkThread(int workThread) {
            this.workThread = workThread;
            return this;
        }

        public void mergeFromProperties(Map<String, String> argMap) {
            String name = argMap.get("name");
            if (StringUtil.nonEmpty(name)) {
                setName(name);
            }

            String portBind = argMap.get("port.bind");
            if (StringUtil.nonEmpty(portBind)) {
                setPort(Integer.parseInt(portBind));
            }
            String portAutoIncrease = argMap.get("port.autoIncrease");
            if (StringUtil.nonEmpty(portAutoIncrease)) {
                setAutoIncrease(Boolean.parseBoolean(portAutoIncrease));
            }

            String httpMaxChunkSize = argMap.get("http.maxChunkSize");
            if (StringUtil.nonEmpty(httpMaxChunkSize)) {
                setHttpMaxChunkSize(Integer.parseInt(httpMaxChunkSize));
            }

            String securityEnable = argMap.get("security.enable");
            if (StringUtil.nonEmpty(securityEnable)) {
                setSecurityEnable(Boolean.parseBoolean(securityEnable));
            }
            String securityAccount = argMap.get("security.account");
            if (StringUtil.nonEmpty(securityAccount)) {
                setSecurityAccount(securityAccount);
            }
            String securityPassword = argMap.get("security.password");
            if (StringUtil.nonEmpty(securityPassword)) {
                setSecurityPassword(securityPassword);
            }

            String logLevel = argMap.get("log.level");
            if (StringUtil.nonEmpty(logLevel)) {
                setLogLevel(logLevel);
            }

            String workThread = argMap.get("workThread");
            if (StringUtil.nonEmpty(workThread)) {
                setWorkThread(Math.max(Integer.parseInt(workThread), 1));
            }

        }

        public void mergeFromMapping(ConfigFileMapping mapping) {
            if (StringUtil.nonEmpty(mapping.getName())) {
                setName(mapping.getName());
            }
            String portStr = mapping.getPort().get("bind");
            if (StringUtil.nonEmpty(portStr)) {
                setPort(Integer.parseInt(portStr));
            }
            String autoIncreaseStr = mapping.getPort().get("autoIncrease");
            if (StringUtil.nonEmpty(autoIncreaseStr)) {
                setAutoIncrease(Boolean.parseBoolean(autoIncreaseStr));
            }

            String maxChunkSize = mapping.getHttp().get("maxChunkSize");
            if (StringUtil.nonEmpty(maxChunkSize)) {
                setHttpMaxChunkSize(Integer.parseInt(maxChunkSize));
            }

            String account = mapping.getSecurity().get("account");
            if (StringUtil.nonEmpty(account)) {
                setSecurityAccount(account);
            }

            String password = mapping.getSecurity().get("password");
            if (StringUtil.nonEmpty(password)) {
                setSecurityPassword(password);
            }

            String enable = mapping.getSecurity().get("enable");
            if (StringUtil.nonEmpty(enable)) {
                setSecurityEnable(Boolean.parseBoolean(enable));
            }

            Map<String, String> logConfig = mapping.getLog();
            String logLevel = logConfig.get("level");
            if (StringUtil.nonEmpty(logLevel)) {
                setLogLevel(logLevel);
            }

            if (StringUtil.nonEmpty(mapping.getWorkThread())) {
                setWorkThread(Math.max(Integer.parseInt(mapping.getWorkThread()), 1));
            }
        }
    }

    public int getPort() {
        return port;
    }

    public boolean isAutoIncrease() {
        return autoIncrease;
    }

    public String getName() {
        return name;
    }

    public int getHttpMaxChunkSize() {
        return httpMaxChunkSize;
    }

    public boolean isSecurityEnable() {
        return securityEnable;
    }

    public String getSecurityAccount() {
        return securityAccount;
    }

    public String getSecurityPassword() {
        return securityPassword;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public int getWorkThread() {
        return workThread;
    }
}
