package org.beifengtz.jvmm.log.printer;

/**
 * @author beifengtz
 * @description: TODO
 * @date 10:26 2023/2/3
 */
public interface Printer {

    void print(Object content);

    boolean ignoreAnsi();

    /**
     * 是否需要提前格式化
     * @return true-日志信息格式化为string false-传输LoggerEvent
     */
    boolean preformat();
}
