package org.beifengtz.jvmm.server.entity.conf;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:33 2022/9/7
 *
 * @author beifengtz
 */
public class SentinelSubscriberConf {
    private String url;
    private AuthOptionConf auth;

    public String getUrl() {
        return url;
    }

    public SentinelSubscriberConf setUrl(String url) {
        this.url = url;
        return this;
    }

    public AuthOptionConf getAuth() {
        return auth;
    }

    public SentinelSubscriberConf setAuth(AuthOptionConf auth) {
        this.auth = auth;
        return this;
    }
}
