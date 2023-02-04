package org.beifengtz.jvmm.server.entity.conf;

import org.beifengtz.jvmm.common.logger.LoggerLevel;
import org.beifengtz.jvmm.common.util.meta.PairKey;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:44 2022/9/7
 *
 * @author beifengtz
 */
public class LogConf {
    private LoggerLevel level;
    private String file;
    private String fileName;
    private Integer fileLimitSize;
    private String pattern;
    private String printers;

    public LoggerLevel getLevel() {
        return level;
    }

    public LogConf setLevel(LoggerLevel level) {
        this.level = level;
        return this;
    }

    public String getFile() {
        return file;
    }

    public LogConf setFile(String file) {
        this.file = file;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public LogConf setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public int getFileLimitSize() {
        return fileLimitSize;
    }

    public LogConf setFileLimitSize(int fileLimitSize) {
        this.fileLimitSize = fileLimitSize;
        return this;
    }

    public String getPattern() {
        return pattern;
    }

    public LogConf setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public String getPrinters() {
        return printers;
    }

    public LogConf setPrinters(String printers) {
        this.printers = printers;
        return this;
    }

    public void setSystemProperties() {
        if (level != null) {
            System.setProperty("jvmm.log.level", level.toString());
        }

        if (file != null) {
            System.setProperty("jvmm.log.file", file);
        }

        if (fileName != null) {
            System.setProperty("jvmm.log.fileName", fileName);
        }

        if (fileLimitSize != null) {
            System.setProperty("jvmm.log.fileLimitSize", fileLimitSize.toString());
        }

        if (pattern != null) {
            System.setProperty("jvmm.log.pattern", pattern);
        }

        if (printers != null) {
            System.setProperty("jvmm.log.printers", printers);
        }
    }
}
