package org.beifengtz.jvmm.web.common.exception;

/**
 * Description: TODO
 *
 * Created in 15:53 2022/1/11
 *
 * @author beifengtz
 */
public class AuthException extends RuntimeException {

    public AuthException() {
    }

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthException(Throwable cause) {
        super(cause);
    }

    public AuthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
