package org.beifengtz.jvmm.common.logger;

/**
 * Description: TODO
 *
 * Created in 16:57 2021/12/9
 *
 * @author beifengtz
 */
public enum LoggerLevel {
    ERROR(1), WARN(2), INFO(3), DEBUG(4), TRACE(5), OFF(6);

    private final int value;

    LoggerLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
