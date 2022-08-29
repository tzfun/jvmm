package org.beifengtz.jvmm.common.exception;

/**
 * Description: TODO
 *
 * Created in 14:22 2022/2/25
 *
 * @author beifengtz
 */
public class JvmmConnectFailedException extends RuntimeException {
    public JvmmConnectFailedException() {
    }

    public JvmmConnectFailedException(String message) {
        super(message);
    }

    public JvmmConnectFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public JvmmConnectFailedException(Throwable cause) {
        super(cause);
    }

    public JvmmConnectFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
