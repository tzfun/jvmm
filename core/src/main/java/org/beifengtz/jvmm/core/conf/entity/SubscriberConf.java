package org.beifengtz.jvmm.core.conf.entity;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:33 2022/9/7
 *
 * @author beifengtz
 */
public class SubscriberConf {
    private String url;
    private AuthOptionConf auth;

    public String getUrl() {
        return url;
    }

    public SubscriberConf setUrl(String url) {
        this.url = url;
        return this;
    }

    public AuthOptionConf getAuth() {
        return auth;
    }

    public SubscriberConf setAuth(AuthOptionConf auth) {
        this.auth = auth;
        return this;
    }
}
