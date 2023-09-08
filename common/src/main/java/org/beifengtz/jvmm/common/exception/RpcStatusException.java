package org.beifengtz.jvmm.common.exception;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 8:22 下午 2021/12/13
 *
 * @author beifengtz
 */
public class RpcStatusException extends RuntimeException {

    private final String status;

    public RpcStatusException(String status) {
        this.status = status;
    }

    public RpcStatusException(String message, String status) {
        super(message);
        this.status = status;
    }

    public RpcStatusException(String message, Throwable cause, String status) {
        super(message, cause);
        this.status = status;
    }

    public RpcStatusException(Throwable cause, String status) {
        super(cause);
        this.status = status;
    }

    public RpcStatusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String status) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
