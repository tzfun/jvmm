package org.beifengtz.jvmm.common.logger;

/**
 * Description: TODO
 *
 * Created in 16:57 2021/12/9
 *
 * @author beifengtz
 */
public enum LoggerLevel {
    OFF(1),
    FATAL(2),
    ERROR(3),
    WARN(4),
    INFO(5),
    DEBUG(6),
    TRACE(7),
    ALL(8);

    private final int value;

    LoggerLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
