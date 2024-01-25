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

    public enum SubscriberType {
        /**
         * 默认的 HTTP 协议订阅
         */
        http,
        /**
         * prometheus 协议订阅
         */
        prometheus
    }

    private SubscriberType type = SubscriberType.http;
    private String url;
    private AuthOptionConf auth;

    public String getUrl() {
        return url;
    }

    public SubscriberType getType() {
        return type;
    }

    public SentinelSubscriberConf setType(SubscriberType type) {
        this.type = type;
        return this;
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
