package org.beifengtz.jvmm.tools.util;

import java.lang.management.ManagementFactory;
import java.util.List;

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
    public static long findProcessByPort(int port) {
        long processId = -1;
        if (PlatformUtil.isWindows()) {
            List<String> result = ExecuteNativeUtil.execute("netstat -aon");
            for (String line : result) {
                if (line.contains(String.valueOf(port))) {
                    String[] split = line.split("(\\s+)|(\r+)|(\n+)");
                    if (split.length > 2) {
                        int port1 = Integer.parseInt(split[2].substring(split[2].lastIndexOf(":") + 1));
                        if (port == port1 && split.length > 5) {
                            processId = Long.parseLong(split[5]);
                            break;
                        }
                    }
                }
            }
        } else if (PlatformUtil.isMac() || PlatformUtil.isLinux()) {
            String command = "lsof -t -s -i:" + port;
            String s = ExecuteNativeUtil.executeForFirstLine(command);
            if (!StringUtil.isEmpty(s)) {
                processId = Long.parseLong(s);
            }
        }

        return processId;
    }
}
