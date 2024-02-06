package org.beifengtz.jvmm.log;

import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.util.SystemPropertyUtil;
import org.beifengtz.jvmm.log.printer.AgentProxyPrinter;
import org.beifengtz.jvmm.log.printer.FilePrinter;
import org.beifengtz.jvmm.log.printer.Printer;
import org.beifengtz.jvmm.log.printer.StdPrinter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: TODO
 * date 10:01 2023/2/3
 *
 * @author beifengtz
 */
public class JvmmLoggerFactory extends InternalLoggerFactory {
    protected static ConcurrentHashMap<String, JvmmLogger> loggerMap = new ConcurrentHashMap<>();
    static volatile JvmmLoggerFactory INSTANCE;

    protected final List<Printer> printers = new ArrayList<>(2);

    private JvmmLogConfiguration config;

    protected JvmmLoggerFactory() {
        loadConfig();
    }

    public static JvmmLoggerFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (JvmmLoggerFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new JvmmLoggerFactory();
                }
            }
        }
        return INSTANCE;
    }

    private void loadConfig() {
        config = new JvmmLogConfiguration();
        String level = SystemPropertyUtil.get(SystemPropertyUtil.PROPERTY_JVMM_LOG_LEVEL);
        if (level != null) {
            config.setLevel(InternalLogLevel.valueOf(level.toUpperCase()));
        }

        String file = SystemPropertyUtil.get(SystemPropertyUtil.PROPERTY_JVMM_LOG_FILE);
        if (file != null) {
            File f = new File(file);
            if (!f.exists()) {
                f.mkdirs();
            }
            config.setFile(f.getAbsolutePath());
        }

        String fileName = SystemPropertyUtil.get(SystemPropertyUtil.PROPERTY_JVMM_LOG_FILE_NAME);
        if (fileName != null) {
            config.setFileName(fileName);
        }

        String fileLimitSize = SystemPropertyUtil.get(SystemPropertyUtil.PROPERTY_JVMM_LOG_FILE_LIMIT_SIZE);
        if (fileLimitSize != null) {
            config.setFileLimitSize(Integer.parseInt(fileLimitSize));
        }

        String pattern = SystemPropertyUtil.get(SystemPropertyUtil.PROPERTY_JVMM_LOG_PATTERN);
        if (pattern != null) {
            config.setPattern(pattern);
        }

        String printers = SystemPropertyUtil.get(SystemPropertyUtil.PROPERTY_JVMM_LOG_PRINTERS);
        if (printers != null) {
            config.setPrinters(printers);
        }

        String levels = SystemPropertyUtil.get(SystemPropertyUtil.PROPERTY_JVMM_LOG_LEVELS);
        if (levels != null) {
            for (String kv : levels.split(",")) {
                String[] split = kv.split(":");
                String key = split[0];
                InternalLogLevel value = InternalLogLevel.valueOf(split[1].toUpperCase());
                config.getLevels().put(key, value);
            }
        }

        updatePrinters();
    }

    public void updatePrinters() {
        printers.clear();
        String ps = config.getPrinters();
        for (String p : ps.split(",")) {
            if ("std".equalsIgnoreCase(p.trim())) {
                printers.add(new StdPrinter());
            } else if ("file".equalsIgnoreCase(p.trim())) {
                printers.add(new FilePrinter(config));
            } else if ("agentProxy".equalsIgnoreCase(p.trim())) {
                printers.add(new AgentProxyPrinter());
            }
        }
        clearLoggerCache();
    }

    public void clearLoggerCache() {
        loggerMap.clear();
    }

    public JvmmLogConfiguration getConfig() {
        return config;
    }

    @Override
    protected JvmmLogger newInstance(String name) {
        JvmmLogger logger = loggerMap.get(name);
        if (logger != null) {
            return logger;
        } else {
            JvmmLogger newInstance = new JvmmLogger(name);
            newInstance.addAllPrinter(printers);
            JvmmLogger oldInstance = loggerMap.putIfAbsent(name, newInstance);
            return oldInstance == null ? newInstance : oldInstance;
        }
    }

    public static InternalLogger getInstance(String name) {
        return getInstance().newInstance(name);
    }
}
