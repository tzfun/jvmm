package org.beifengtz.jvmm.convey.enums;

import java.util.HashMap;
import java.util.Map;

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
    private static final Map<Integer, RpcStatus> VALUE_MAP = new HashMap<>();

    RpcStatus(int value) {
        this.value = value;
        mapping();
    }

    void mapping() {
        VALUE_MAP.put(value, this);
    }

    public int getValue() {
        return value;
    }

    public static RpcStatus valueOf(int value) {
        return VALUE_MAP.get(value);
    }
}
