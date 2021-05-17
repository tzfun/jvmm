package org.beifengtz.jvmm.common.exception;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 19:21 2021/5/17
 *
 * @author beifengtz
 */
public class SecurityErrorException extends RuntimeException{

    public SecurityErrorException() {
    }

    public SecurityErrorException(String message) {
        super(message);
    }

    public SecurityErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityErrorException(Throwable cause) {
        super(cause);
    }

    public SecurityErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
