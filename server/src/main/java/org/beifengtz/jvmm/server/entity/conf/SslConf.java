package org.beifengtz.jvmm.server.entity.conf;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:30 2022/9/7
 *
 * @author beifengtz
 */
public class SslConf {
    private boolean enable;
    private String certCa;
    private String cert;
    private String certKey;
    private String keyPassword;
    private boolean openssl = true;

    public boolean isEnable() {
        return enable;
    }

    public SslConf setEnable(boolean enable) {
        this.enable = enable;
        return this;
    }

    public String getCertCa() {
        return certCa;
    }

    public SslConf setCertCa(String certCa) {
        this.certCa = certCa;
        return this;
    }

    public String getCert() {
        return cert;
    }

    public SslConf setCert(String cert) {
        this.cert = cert;
        return this;
    }

    public String getCertKey() {
        return certKey;
    }

    public SslConf setCertKey(String certKey) {
        this.certKey = certKey;
        return this;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public SslConf setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
        return this;
    }

    public boolean isOpenssl() {
        return openssl;
    }

    public SslConf setOpenssl(boolean openssl) {
        this.openssl = openssl;
        return this;
    }
}
