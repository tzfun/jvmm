package org.beifengtz.jvmm.core.conf.entity;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:24 2022/9/7
 *
 * @author beifengtz
 */
public class ServerConf {
    private String type = "jvmm";
    private JvmmServerConf jvmm = new JvmmServerConf();
    private HttpServerConf http;
    private SentinelConf sentinel;

    public String getType() {
        return type;
    }

    public ServerConf setType(String type) {
        this.type = type;
        return this;
    }

    public JvmmServerConf getJvmm() {
        return jvmm;
    }

    public ServerConf setJvmm(JvmmServerConf jvmm) {
        this.jvmm = jvmm;
        return this;
    }

    public HttpServerConf getHttp() {
        return http;
    }

    public ServerConf setHttp(HttpServerConf http) {
        this.http = http;
        return this;
    }

    public SentinelConf getSentinel() {
        return sentinel;
    }

    public ServerConf setSentinel(SentinelConf sentinel) {
        this.sentinel = sentinel;
        return this;
    }
}
