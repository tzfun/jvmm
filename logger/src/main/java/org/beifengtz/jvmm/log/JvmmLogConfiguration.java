package org.beifengtz.jvmm.log;

import io.netty.util.internal.logging.InternalLogLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * description: TODO
 * date 10:18 2023/2/3
 * @author beifengtz
 */
public class JvmmLogConfiguration {

    private static final String DEFAULT_PATTERN = "%ansi{%date{yyyy-MM-dd HH:mm:ss}}{36} %ansi{%level}{ERROR=31,INFO=32,WARN=33,DEBUG=34,TRACE=35} %ansi{%class}{38;5;14}\t: %msg";
    /**
     * 日志等级
     */
    private InternalLogLevel level = InternalLogLevel.INFO;
    private Map<String,InternalLogLevel> levels = new HashMap<>(0);
    /**
     * 日志文件输出目录
     */
    private String file = "logs";
    /**
     * 日志文件名
     */
    private String fileName = "jvmm";
    /**
     * 当前日志文件大小超过此值就生成新的日志文件，单位MB
     */
    private int fileLimitSize = 10;
    /**
     * 日志匹配规则
     */
    private String pattern = DEFAULT_PATTERN;
    /**
     * 支持的输出类型
     */
    private String printers = "std,file";

    public InternalLogLevel getLevel() {
        return level;
    }

    public void setLevel(InternalLogLevel level) {
        this.level = level;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileLimitSize() {
        return fileLimitSize;
    }

    public void setFileLimitSize(int fileLimitSize) {
        this.fileLimitSize = fileLimitSize;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPrinters() {
        return printers;
    }

    public void setPrinters(String printers) {
        this.printers = printers;
    }

    public Map<String, InternalLogLevel> getLevels() {
        return levels;
    }

    public JvmmLogConfiguration setLevels(Map<String, InternalLogLevel> levels) {
        this.levels = levels;
        return this;
    }
}
