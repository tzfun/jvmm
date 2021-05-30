package org.beifengtz.jvmm.common.exception;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:57 下午 2021/5/29
 *
 * @author beifengtz
 */
public class SocketExecuteException extends RuntimeException{

    public SocketExecuteException() {
    }

    public SocketExecuteException(String message) {
        super(message);
    }

    public SocketExecuteException(String message, Throwable cause) {
        super(message, cause);
    }

    public SocketExecuteException(Throwable cause) {
        super(cause);
    }

    public SocketExecuteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
