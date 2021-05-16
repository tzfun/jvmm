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
public final class ClientConfiguration {

    private final String name;
    private final int port;
    private final boolean autoIncrease;

    private ClientConfiguration(Builder builder) {
        this.name = builder.name;
        this.port = builder.port;
        this.autoIncrease = builder.autoIncrease;
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
        sb.append("port.autoIncrease=").append(autoIncrease);
        return sb.toString();
    }

    static class Builder {
        private String name = "jvmm_client";
        private int port = 5010;
        private boolean autoIncrease = true;

        public ClientConfiguration build() {
            return new ClientConfiguration(this);
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

        public void merge(ClientConfigFileMapping mapping) {
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
}
