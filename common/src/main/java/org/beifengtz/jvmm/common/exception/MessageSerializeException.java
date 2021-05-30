package org.beifengtz.jvmm.common.exception;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 9:55 下午 2021/5/29
 *
 * @author beifengtz
 */
public class MessageSerializeException extends RuntimeException {

    public MessageSerializeException() {
    }

    public MessageSerializeException(String message) {
        super(message);
    }

    public MessageSerializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageSerializeException(Throwable cause) {
        super(cause);
    }

    public MessageSerializeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
