package org.beifengtz.jvmm.client;

import com.google.gson.Gson;

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

    private Configuration(Builder builder) {
        this.name = builder.name;
        this.port = builder.port;
        this.autoIncrease = builder.autoIncrease;
        this.httpMaxChunkSize = builder.httpMaxChunkSize;
        this.securityEnable = builder.securityEnable;
        this.securityAccount = builder.securityAccount;
        this.securityPassword = builder.securityPassword;
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
        if (securityAccount != null) {
            sb.append("security.account=").append(securityAccount).append(";");
        }
        if (securityPassword != null) {
            sb.append("security.password=").append(securityPassword).append(";");
        }
        sb.append("security.enable=").append(securityEnable);
        return sb.toString();
    }

    static class Builder {
        private String name = "jvmm_client";
        private int port = 5010;
        private boolean autoIncrease = true;
        private int httpMaxChunkSize = 52428800;   //  50MB
        private boolean securityEnable = false;
        private String securityAccount = null;
        private String securityPassword = null;

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

        public void merge(ConfigFileMapping mapping) {
            if (mapping.getName() != null) {
                setName(mapping.getName());
            }
            String portStr = mapping.getPort().get("bind");
            if (portStr != null) {
                setPort(Integer.parseInt(portStr));
            }
            String autoIncreaseStr = mapping.getPort().get("autoIncrease");
            if (autoIncreaseStr != null) {
                setAutoIncrease(Boolean.parseBoolean(autoIncreaseStr));
            }

            String maxChunkSize = mapping.getHttp().get("maxChunkSize");
            if (maxChunkSize != null) {
                setHttpMaxChunkSize(Integer.parseInt(maxChunkSize));
            }

            String account = mapping.getSecurity().get("account");
            if (account != null) {
                setSecurityAccount(account);
            }

            String password = mapping.getSecurity().get("password");
            if (password != null) {
                setSecurityPassword(password);
            }

            String enable = mapping.getSecurity().get("enable");
            if (enable != null) {
                setSecurityEnable(Boolean.parseBoolean(enable));
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
}
