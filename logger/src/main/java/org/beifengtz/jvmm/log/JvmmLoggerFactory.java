package org.beifengtz.jvmm.log;

import org.beifengtz.jvmm.common.logger.LoggerLevel;
import org.beifengtz.jvmm.common.util.SystemPropertyUtil;
import org.beifengtz.jvmm.log.printer.AgentProxyPrinter;
import org.beifengtz.jvmm.log.printer.FilePrinter;
import org.beifengtz.jvmm.log.printer.Printer;
import org.beifengtz.jvmm.log.printer.StdPrinter;
import org.slf4j.ILoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: TODO
 * date 10:01 2023/2/3
 * @author beifengtz
 */
public class JvmmLoggerFactory implements ILoggerFactory {
    static ConcurrentHashMap<String, Logger> loggerMap = new ConcurrentHashMap<>();

    private final List<Printer> printers = new ArrayList<>(2);

    private JvmmLogConfiguration config;

    {
        loadConfig();
    }

    private void loadConfig() {
        config = new JvmmLogConfiguration();
        String level = SystemPropertyUtil.get("jvmm.log.level");
        if (level != null) {
            config.setLevel(LoggerLevel.valueOf(level.toUpperCase()));
        }

        String file = SystemPropertyUtil.get("jvmm.log.file");
        if (file != null) {
            File f = new File(file);
            if (!f.exists()) {
                f.mkdirs();
            }
            config.setFile(f.getAbsolutePath());
        }

        String fileName = SystemPropertyUtil.get("jvmm.log.fileName");
        if (fileName != null) {
            config.setFileName(fileName);
        }

        String fileLimitSize = SystemPropertyUtil.get("jvmm.log.fileLimitSize");
        if (fileLimitSize != null) {
            config.setFileLimitSize(Integer.parseInt(fileLimitSize));
        }

        String pattern = SystemPropertyUtil.get("jvmm.log.pattern");
        if (pattern != null) {
            config.setPattern(pattern);
        }

        String printers = SystemPropertyUtil.get("jvmm.log.printers");
        if (printers != null) {
            config.setPrinters(printers);
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

    @Override
    public org.slf4j.Logger getLogger(String name) {
        Logger logger = loggerMap.get(name);
        if (logger != null) {
            return logger;
        } else {
            Logger newInstance = new Logger(name);
            newInstance.addAllPrinter(printers);
            Logger oldInstance = loggerMap.putIfAbsent(name, newInstance);
            return oldInstance == null ? newInstance : oldInstance;
        }
    }

    public JvmmLogConfiguration getConfig() {
        return config;
    }
}
