package org.beifengtz.jvmm.web.mvc.service;

import lombok.extern.slf4j.Slf4j;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.web.common.exception.AuthException;
import org.beifengtz.jvmm.web.mvc.controller.JvmmController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Locale;
import java.util.Objects;

/**
 * Description: Jvmm业务逻辑实现类
 *
 * Created in 15:13 2022/1/11
 *
 * @author beifengtz
 */
@Service
@Slf4j
public class JvmmServiceImpl {
    private static final String TOKEN_SPLITTER = ":";

    private volatile String token;

    @Value("${jvmm.username}")
    private String jvmmUsername;
    @Value("${jvmm.password}")
    private String jvmmPassword;

    @Resource
    private JvmmController jvmmController;

    public String login(String username, String password) {
        if (Objects.equals(jvmmUsername, username) && Objects.equals(jvmmPassword, password.toLowerCase(Locale.ROOT))) {
            token = StringUtil.genUUID(false) + TOKEN_SPLITTER + System.currentTimeMillis();
            return token;
        }
        throw new AuthException();
    }

    public void verifyToken(String token) {
        if (!Objects.equals(token, this.token)) {
            throw new AuthException();
        }
    }
}
