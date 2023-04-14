package org.beifengtz.jvmm.log.printer;

import org.beifengtz.jvmm.log.JvmmLogConfiguration;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description: 文件输出实现
 * date 10:25 2023/2/3
 * @author beifengtz
 */
public class FilePrinter implements Printer {

    private final AtomicInteger sequenceFlag = new AtomicInteger(0);

    public FilePrinter(JvmmLogConfiguration config) {
        File logDir = new File(config.getFile());
        File[] files = logDir.listFiles(o -> o.getName().matches(config.getFileName() + "-\\d+\\.log"));
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                int num = Integer.parseInt(fileName.substring(fileName.lastIndexOf("-") + 1, fileName.lastIndexOf(".")));
                sequenceFlag.set(Math.max(sequenceFlag.get(), num));
            }
        }
    }

    @Override
    public synchronized void print(Object content) {
        JvmmLogConfiguration config = StaticLoggerBinder.getSingleton().getConfig();
        File file = new File(config.getFile(), config.getFileName() + ".log");
        try {
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(file, true), true)) {
                pw.write(content + "\n");
            }
            if (file.length() >= config.getFileLimitSize() * 1024 * 1024L) {
                File newFile = new File(file.getParentFile(), config.getFileName() + "-" + sequenceFlag.incrementAndGet() + ".log");
                file.renameTo(newFile);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public boolean ignoreAnsi() {
        return true;
    }

    @Override
    public boolean preformat() {
        return true;
    }
}
