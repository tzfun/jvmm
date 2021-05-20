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
    private final String logPath;
    private final String logPattern;
    private final String logFileName;
    private final String logMaxFileSize;

    private Configuration(Builder builder) {
        this.name = builder.name;
        this.port = builder.port;
        this.autoIncrease = builder.autoIncrease;
        this.httpMaxChunkSize = builder.httpMaxChunkSize;
        this.securityEnable = builder.securityEnable;
        this.securityAccount = builder.securityAccount;
        this.securityPassword = builder.securityPassword;
        this.logLevel = builder.logLevel;
        this.logPath = builder.logPath;
        this.logPattern = builder.logPattern;
        this.logFileName = builder.logFileName;
        this.logMaxFileSize = builder.logMaxFileSize;
    }

    public static Builder newBuilder() {
        return new Builder();
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
        sb.append("log.path=").append(logPath).append(";");
        sb.append("log.pattern=").append(logPattern).append(";");
        sb.append("log.fileName=").append(logFileName).append(";");
        sb.append("log.maxFileSize=").append(logMaxFileSize).append(";");
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
        private String logPath = "logs/";
        private String logPattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%-5level] %logger{50} - %msg%n";
        private String logFileName = "jvmm-server.log";
        private String logMaxFileSize = "10MB";

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

        public void setHttpMaxChunkSize(int httpMaxChunkSize) {
            this.httpMaxChunkSize = httpMaxChunkSize;
        }

        public boolean isSecurityEnable() {
            return securityEnable;
        }

        public void setSecurityEnable(boolean securityEnable) {
            this.securityEnable = securityEnable;
        }

        public String getSecurityAccount() {
            return securityAccount;
        }

        public void setSecurityAccount(String securityAccount) {
            this.securityAccount = securityAccount;
        }

        public String getSecurityPassword() {
            return securityPassword;
        }

        public void setSecurityPassword(String securityPassword) {
            this.securityPassword = securityPassword;
        }

        public String getLogLevel() {
            return logLevel;
        }

        public void setLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }

        public String getLogPath() {
            return logPath;
        }

        public void setLogPath(String logPath) {
            this.logPath = logPath;
        }

        public String getLogPattern() {
            return logPattern;
        }

        public void setLogPattern(String logPattern) {
            this.logPattern = logPattern;
        }

        public String getLogFileName() {
            return logFileName;
        }

        public void setLogFileName(String logFileName) {
            this.logFileName = logFileName;
        }

        public String getLogMaxFileSize() {
            return logMaxFileSize;
        }

        public void setLogMaxFileSize(String logMaxFileSize) {
            this.logMaxFileSize = logMaxFileSize;
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
            String logPath = argMap.get("log.path");
            if (StringUtil.nonEmpty(logPath)) {
                setLogPath(logPath);
            }
            String logPattern = argMap.get("log.pattern");
            if (StringUtil.nonEmpty(logPattern)) {
                setLogPattern(logPattern);
            }
            String logFileName = argMap.get("log.fileName");
            if (StringUtil.nonEmpty(logFileName)) {
                setLogFileName(logFileName);
            }
            String logMaxFileSize = argMap.get("log.maxFileSize");
            if (StringUtil.nonEmpty(logMaxFileSize)) {
                setLogMaxFileSize(logMaxFileSize);
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

            String logPattern = logConfig.get("pattern");
            if (StringUtil.nonEmpty(logPattern)) {
                setLogPath(logPattern);
            }

            String logPath = logConfig.get("path");
            if (StringUtil.nonEmpty(logPath)) {
                setLogPath(logPath);
            }

            String logFileName = logConfig.get("fileName");
            if (StringUtil.nonEmpty(logFileName)) {
                setLogPath(logFileName);
            }

            String logMaxFileSize = logConfig.get("maxFileSize");
            if (StringUtil.nonEmpty(logMaxFileSize)) {
                setLogPath(logMaxFileSize);
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

    public String getLogPath() {
        return logPath;
    }

    public String getLogPattern() {
        return logPattern;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public String getLogMaxFileSize() {
        return logMaxFileSize;
    }
}
