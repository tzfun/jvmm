package org.beifengtz.jvmm.common.exception;

/**
 * Description: TODO
 *
 * Created in 11:16 2022/9/29
 *
 * @author beifengtz
 */
public class OsNotSupportedException extends RuntimeException {

    public OsNotSupportedException() {
    }

    public OsNotSupportedException(String message) {
        super(message);
    }

    public OsNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public OsNotSupportedException(Throwable cause) {
        super(cause);
    }

    public OsNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
