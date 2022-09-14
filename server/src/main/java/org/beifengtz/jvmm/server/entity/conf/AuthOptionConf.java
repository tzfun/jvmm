package org.beifengtz.jvmm.server.entity.conf;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:27 2022/9/7
 *
 * @author beifengtz
 */
public class AuthOptionConf {
    private boolean enable;
    private String username;
    private String password;

    public boolean isEnable() {
        return enable;
    }

    public AuthOptionConf setEnable(boolean enable) {
        this.enable = enable;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public AuthOptionConf setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public AuthOptionConf setPassword(String password) {
        this.password = password;
        return this;
    }
}
