package org.beifengtz.jvmm.server.entity.conf;

import io.netty.util.internal.logging.InternalLogLevel;
import org.beifengtz.jvmm.common.util.SystemPropertyUtil;

import java.util.Map;

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
    private InternalLogLevel level;
    private String file;
    private String fileName;
    private Integer fileLimitSize;
    private String pattern;
    private String printers;
    private Map<String, InternalLogLevel> levels;

    public InternalLogLevel getLevel() {
        return level;
    }

    public LogConf setLevel(InternalLogLevel level) {
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

    public LogConf setFileLimitSize(Integer fileLimitSize) {
        this.fileLimitSize = fileLimitSize;
        return this;
    }

    public Map<String, InternalLogLevel> getLevels() {
        return levels;
    }

    public LogConf setLevels(Map<String, InternalLogLevel> levels) {
        this.levels = levels;
        return this;
    }

    public void setSystemProperties() {
        if (level != null) {
            System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_LOG_LEVEL, level.toString());
        }

        if (file != null) {
            System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_LOG_FILE, file);
        }

        if (fileName != null) {
            System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_LOG_FILE_NAME, fileName);
        }

        if (fileLimitSize != null) {
            System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_LOG_FILE_LIMIT_SIZE, fileLimitSize.toString());
        }

        if (pattern != null) {
            System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_LOG_PATTERN, pattern);
        }

        if (printers != null) {
            System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_LOG_PRINTERS, printers);
        }

        if (levels != null && !levels.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            levels.forEach((k, v) -> {
                sb.append(k).append(":").append(v).append(",");
            });
            sb.deleteCharAt(sb.length() - 1);
            System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_LOG_LEVELS, sb.toString());
        }
    }
}
