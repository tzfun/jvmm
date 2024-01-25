package org.beifengtz.jvmm.server.entity.conf;

import java.util.ArrayList;
import java.util.List;

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
    private List<SentinelConf> sentinel = new ArrayList<>();

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

    public List<SentinelConf> getSentinel() {
        return sentinel;
    }

    public ServerConf setSentinel(List<SentinelConf> sentinel) {
        this.sentinel = sentinel;
        return this;
    }

    public ServerConf addSentinel(SentinelConf sentinel) {
        this.sentinel.add(sentinel);
        return this;
    }

    public ServerConf clearSentinel() {
        this.sentinel.clear();
        return this;
    }
}
