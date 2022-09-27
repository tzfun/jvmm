package org.beifengtz.jvmm.common.exception;

/**
 * Description: TODO
 *
 * Created in 18:44 2022/9/23
 *
 * @author beifengtz
 */
public class ExecutionException extends RuntimeException {

    public ExecutionException() {
    }

    public ExecutionException(String message) {
        super(message);
    }

    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExecutionException(Throwable cause) {
        super(cause);
    }

    public ExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
