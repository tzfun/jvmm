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
public class HttpServerConf extends JvmmServerConf {
    private SslConf ssl;

    public SslConf getSsl() {
        return ssl;
    }

    public HttpServerConf setSsl(SslConf ssl) {
        this.ssl = ssl;
        return this;
    }
}
