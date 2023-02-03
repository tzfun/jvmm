package org.beifengtz.jvmm.log.printer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author beifengtz
 * @description: 标准输出实现
 * @date 10:24 2023/2/3
 */
public class StdPrinter implements Printer {

    /**
     * 当前环境是否支持ANSI转译
     */
    private final boolean enableAnsi = Charset.defaultCharset() == StandardCharsets.UTF_8;

    @Override
    public void print(Object content) {
        System.out.println(content);
    }

    @Override
    public boolean ignoreAnsi() {
        return !enableAnsi;
    }

    @Override
    public boolean preformat() {
        return true;
    }
}
