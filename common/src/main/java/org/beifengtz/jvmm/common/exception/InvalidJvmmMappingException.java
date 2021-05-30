package org.beifengtz.jvmm.common.exception;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 6:03 下午 2021/5/30
 *
 * @author beifengtz
 */
public class InvalidJvmmMappingException extends RuntimeException{

    public InvalidJvmmMappingException() {
    }

    public InvalidJvmmMappingException(String message) {
        super(message);
    }

    public InvalidJvmmMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidJvmmMappingException(Throwable cause) {
        super(cause);
    }

    public InvalidJvmmMappingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
