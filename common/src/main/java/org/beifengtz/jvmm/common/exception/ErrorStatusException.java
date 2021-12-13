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
public class ErrorStatusException extends RuntimeException {

    private final String status;

    public ErrorStatusException(String status) {
        this.status = status;
    }

    public ErrorStatusException(String message, String status) {
        super(message);
        this.status = status;
    }

    public ErrorStatusException(String message, Throwable cause, String status) {
        super(message, cause);
        this.status = status;
    }

    public ErrorStatusException(Throwable cause, String status) {
        super(cause);
        this.status = status;
    }

    public ErrorStatusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String status) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
