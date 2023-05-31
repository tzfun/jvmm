package org.beifengtz.jvmm.server.entity.conf;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:25 2022/9/7
 *
 * @author beifengtz
 */
@SuppressWarnings("unchecked")
public class JvmmServerConf {
    private int port = 5010;
    private boolean adaptivePort = true;
    private int adaptivePortLimit = 5;
    private AuthOptionConf auth = new AuthOptionConf();
    private int maxChunkSize = 52428800;

    public int getPort() {
        return port;
    }

    public <T extends JvmmServerConf> T setPort(int port) {
        this.port = port;
        return (T)this;
    }

    public boolean isAdaptivePort() {
        return adaptivePort;
    }

    public <T extends JvmmServerConf> T setAdaptivePort(boolean adaptivePort) {
        this.adaptivePort = adaptivePort;
        return (T)this;
    }

    public int getAdaptivePortLimit() {
        return adaptivePortLimit;
    }

    public JvmmServerConf setAdaptivePortLimit(int adaptivePortLimit) {
        this.adaptivePortLimit = adaptivePortLimit;
        return this;
    }

    public AuthOptionConf getAuth() {
        return auth;
    }

    public <T extends JvmmServerConf> T setAuth(AuthOptionConf auth) {
        this.auth = auth;
        return (T)this;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public <T extends JvmmServerConf> T setMaxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
        return (T)this;
    }
}
