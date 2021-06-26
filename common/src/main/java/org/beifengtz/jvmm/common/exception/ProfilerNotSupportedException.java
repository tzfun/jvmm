package org.beifengtz.jvmm.common.exception;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:17 下午 2021/6/26
 *
 * @author beifengtz
 */
public class ProfilerNotSupportedException extends RuntimeException{

    public ProfilerNotSupportedException() {
    }

    public ProfilerNotSupportedException(String message) {
        super(message);
    }

    public ProfilerNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProfilerNotSupportedException(Throwable cause) {
        super(cause);
    }

    public ProfilerNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
