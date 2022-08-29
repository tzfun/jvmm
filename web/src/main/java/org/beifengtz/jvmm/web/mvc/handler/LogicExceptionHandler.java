package org.beifengtz.jvmm.web.mvc.handler;

import lombok.extern.slf4j.Slf4j;
import org.beifengtz.jvmm.common.exception.JvmmConnectFailedException;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.web.common.RestfulStatus;
import org.beifengtz.jvmm.web.common.exception.AuthException;
import org.beifengtz.jvmm.web.entity.vo.ResultVO;
import org.beifengtz.jvmm.web.manage.factory.ResultFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.concurrent.TimeoutException;

/**
 * Description: TODO
 *
 * Created in 16:13 2022/1/12
 *
 * @author beifengtz
 */
@Slf4j
@ControllerAdvice
public class LogicExceptionHandler {

    @Resource
    private ResultFactory resultFactory;

    @ExceptionHandler(value = Throwable.class)
    @ResponseBody
    public ResultVO<?> ServerErrHandler(Exception e) {
        log.error(e.getMessage(), e);
        return resultFactory.error(StringUtil.emptyOrDefault(e.getMessage(), RestfulStatus.SERVER_ERR.getMsg()));
    }

    @ExceptionHandler(value = AuthException.class)
    @ResponseBody
    public ResultVO<?> authErrHandler(AuthException e) {
        return resultFactory.error(RestfulStatus.AUTH_ERR);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseBody
    public ResultVO<?> illegalArgumentHandler(IllegalArgumentException e) {
        if (!StringUtil.isEmpty(e.getMessage())) {
            log.info("{}; {}", e.getMessage(), e.getClass().getName());
        }
        return resultFactory.error(RestfulStatus.ILLEGAL_ARGUMENT, e.getMessage());
    }

    @ExceptionHandler(value = TimeoutException.class)
    @ResponseBody
    public ResultVO<?> timeoutHandler(TimeoutException e) {
        if (!StringUtil.isEmpty(e.getMessage())) {
            log.info("{}; {}", e.getMessage(), e.getClass().getName());
        }
        return resultFactory.error(RestfulStatus.TIMEOUT, e.getMessage());
    }

    @ExceptionHandler(value = DuplicateKeyException.class)
    @ResponseBody
    public ResultVO<?> duplicateKeyHandler(DuplicateKeyException e) {
        if (!StringUtil.isEmpty(e.getMessage())) {
            log.info("{}; {}", e.getMessage(), e.getClass().getName());
        }
        return resultFactory.error(RestfulStatus.RESOURCE_EXIST, e.getMessage());
    }

    @ExceptionHandler(value = JvmmConnectFailedException.class)
    @ResponseBody
    public ResultVO<?> jvmmConnectFailedHandler(JvmmConnectFailedException e) {
        if (!StringUtil.isEmpty(e.getMessage())) {
            log.debug(e.getMessage(), e);
        }
        return resultFactory.error(RestfulStatus.JVMM_CONNECT_FAILED, e.getMessage());
    }
}
