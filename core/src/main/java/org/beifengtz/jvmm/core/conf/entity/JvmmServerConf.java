package org.beifengtz.jvmm.core.conf.entity;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:25 2022/9/7
 *
 * @author beifengtz
 */
public class JvmmServerConf {
    private int port = 5010;
    private boolean adaptivePort = true;
    private AuthOptionConf auth = new AuthOptionConf();
    private int maxChunkSize = 52428800;

    public int getPort() {
        return port;
    }

    public JvmmServerConf setPort(int port) {
        this.port = port;
        return this;
    }

    public boolean isAdaptivePort() {
        return adaptivePort;
    }

    public JvmmServerConf setAdaptivePort(boolean adaptivePort) {
        this.adaptivePort = adaptivePort;
        return this;
    }

    public AuthOptionConf getAuth() {
        return auth;
    }

    public JvmmServerConf setAuth(AuthOptionConf auth) {
        this.auth = auth;
        return this;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public JvmmServerConf setMaxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
        return this;
    }
}
