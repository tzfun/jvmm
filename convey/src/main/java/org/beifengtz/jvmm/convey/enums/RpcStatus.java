package org.beifengtz.jvmm.convey.enums;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 8:43 下午 2021/5/17
 *
 * @author beifengtz
 */
public enum RpcStatus {
    JVMM_STATUS_OK(0),
    JVMM_STATUS_SERVER_ERROR(10001),
    JVMM_STATUS_UNRECOGNIZED_CONTENT(10002),
    JVMM_STATUS_AUTHENTICATION_FAILED(10003),
    JVMM_STATUS_ILLEGAL_ARGUMENTS(10004),
    JVMM_STATUS_EXECUTE_FAILED(10005),
    JVMM_STATUS_PROFILER_FAILED(10006);

    private final int value;

    RpcStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static RpcStatus valueOf(int value) {
        switch (value){
            case 0: return JVMM_STATUS_OK;
            case 10001: return JVMM_STATUS_SERVER_ERROR;
            case 10002: return JVMM_STATUS_UNRECOGNIZED_CONTENT;
            case 10003: return JVMM_STATUS_AUTHENTICATION_FAILED;
            case 10004: return JVMM_STATUS_ILLEGAL_ARGUMENTS;
            case 10005: return JVMM_STATUS_EXECUTE_FAILED;
            case 10006: return JVMM_STATUS_PROFILER_FAILED;
            default: return null;
        }
    }
}
