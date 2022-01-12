package org.beifengtz.jvmm.web.common;

/**
 * Description: TODO
 *
 * Created in 15:30 2022/1/12
 *
 * @author beifengtz
 */
public enum RestfulStatus {
    SERVER_ERR(-1, "Server error"),
    OK(10000, "OK"),
    AUTH_ERR(10001, "Authentication failed"),
    OPERATION_FREQUENT(10002, "The operation is too frequent"),
    ILLEGAL_ARGUMENT(10003, "Illegal argument."),
    TIMEOUT(10004, "Connect timeout"),
    RESOURCE_EXIST(10005,"Resource has exist"),
    RESOURCE_NOT_FOUND(10006,"Resource not found")
    ;

    private final int status;
    private final String msg;

    RestfulStatus(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }
}
