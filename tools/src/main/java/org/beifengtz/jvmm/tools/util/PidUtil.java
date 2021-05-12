package org.beifengtz.jvmm.tools.util;

import java.lang.management.ManagementFactory;

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
}
