package org.beifengtz.jvmm.tools.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Scanner;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 15:35 2021/5/12
 *
 * @author beifengtz
 */
public class PidUtil {
    private static long pid = -1;

    static {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        int index = jvmName.indexOf("@");
        if (index > 0) {
            pid = Long.parseLong(jvmName.substring(0, index));
        }
    }

    public static long currentPid() {
        return pid;
    }

    /**
     * 根据端口查询pid
     *
     * @param port 端口
     * @return 进程pid，如未找到返回-1
     */
    public static long findProcess(int port) {
        long processId = -1;
        try {
            if (PlatformUtil.isWindows()) {
                String command = "netstat -aon";
                Process process = Runtime.getRuntime().exec(command);
                try (InputStream is = process.getInputStream(); Scanner sc = new Scanner(is, PlatformUtil.getEncoding())) {
                    while (sc.hasNext()) {
                        String line = sc.nextLine();
                        if (line.contains(String.valueOf(port))) {
                            String[] split = line.split("(\\s+)|(\r+)|(\n+)");
                            if (split.length > 2) {
                                int port1 = Integer.parseInt(split[2].split(":")[1]);

                                if (port == port1 && split.length > 5) {
                                    processId = Long.parseLong(split[5]);
                                    break;
                                }
                            }
                        }
                    }
                }
                handleProcessError(process, command);
            } else if (PlatformUtil.isMac() || PlatformUtil.isLinux()) {
                String command = "lsof -i:" + port;
                Process process = Runtime.getRuntime().exec(command);
                try (InputStream is = process.getInputStream(); Scanner sc = new Scanner(is, PlatformUtil.getEncoding())) {
                    while (sc.hasNext()) {
                        String line = sc.nextLine();
                        if (line.contains(String.valueOf(port))) {
                            String[] split = line.split("(\\s+)|(\r+)|(\n+)");
                            if (split.length > 8) {
                                int port1 = Integer.parseInt(split[8].split(":")[1]);

                                if (port == port1) {
                                    processId = Long.parseLong(split[1]);
                                    break;
                                }
                            }
                        }
                    }
                }
                handleProcessError(process, command);
            }
        } catch (IOException | InterruptedException ignored) {
        }

        return processId;
    }

    private static void handleProcessError(Process process, String command) throws InterruptedException, IOException {
        if (process.waitFor(200, TimeUnit.MILLISECONDS)) {
            int exitVal = process.exitValue();
            if (exitVal < 0) {
                try (InputStream errIs = process.getErrorStream()) {
                    String error = IOUtil.toString(errIs, PlatformUtil.getEncoding());
                    throw new RejectedExecutionException(String.format("Execute command with exit value '%d'. %s. ['%s']"
                            , process.exitValue(), error, command));
                }
            }
        }
    }
}
