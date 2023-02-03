package org.beifengtz.jvmm.common.util;

import org.beifengtz.jvmm.common.PlatformEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Locale;
import java.util.concurrent.RejectedExecutionException;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 15:46 2021/5/12
 *
 * @author beifengtz
 */
public class PlatformUtil {

    private static final Logger log = LoggerFactory.getLogger(PlatformUtil.class);

    private static final String OPERATING_SYSTEM_NAME = SystemPropertyUtil.get("os.name").toLowerCase(Locale.ENGLISH);
    private static final String OPERATING_SYSTEM_ARCH = SystemPropertyUtil.get("os.arch").toLowerCase(Locale.ENGLISH);

    private static final PlatformEnum platform;
    private static final String arch;
    private static final String encoding;

    static {
        if (OPERATING_SYSTEM_NAME.startsWith("linux")) {
            platform = PlatformEnum.LINUX;
        } else if (OPERATING_SYSTEM_NAME.startsWith("mac") || OPERATING_SYSTEM_NAME.startsWith("darwin")) {
            platform = PlatformEnum.MACOS;
        } else if (OPERATING_SYSTEM_NAME.startsWith("windows")) {
            platform = PlatformEnum.WINDOWS;
        } else {
            platform = PlatformEnum.UNKNOWN;
        }

        arch = normalizeArch(OPERATING_SYSTEM_ARCH);
        encoding = SystemPropertyUtil.get("sun.jnu.encoding");
    }

    private PlatformUtil() {
    }

    public static boolean isWindows() {
        return platform == PlatformEnum.WINDOWS;
    }

    public static boolean isLinux() {
        return platform == PlatformEnum.LINUX;
    }

    public static boolean isMac() {
        return platform == PlatformEnum.MACOS;
    }

    public static PlatformEnum getOS() {
        return platform;
    }

    public static String getEncoding() {
        return encoding;
    }

    public static boolean isCygwinOrMinGW() {
        if (isWindows()) {
            return (System.getenv("MSYSTEM") != null && System.getenv("MSYSTEM").startsWith("MINGW"))
                    || "/bin/bash".equals(System.getenv("SHELL"));
        }
        return false;
    }

    public static String arch() {
        return arch;
    }

    public static boolean isArm32() {
        return "arm_32".equals(arch);
    }

    public static boolean isArm64() {
        return "aarch_64".equals(arch);
    }

    public static boolean isX86() {
        return "x86_32".equals(arch);
    }

    public static boolean isX64() {
        return "x86_64".equals(arch);
    }

    public static boolean isAarch64() {
        return "aarch_64".equals(arch);
    }

    private static String normalizeArch(String value) {
        value = normalize(value);
        if (value.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
            return "x86_64";
        }
        if (value.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
            return "x86_32";
        }
        if (value.matches("^(ia64w?|itanium64)$")) {
            return "itanium_64";
        }
        if ("ia64n".equals(value)) {
            return "itanium_32";
        }
        if (value.matches("^(sparc|sparc32)$")) {
            return "sparc_32";
        }
        if (value.matches("^(sparcv9|sparc64)$")) {
            return "sparc_64";
        }
        if (value.matches("^(arm|arm32)$")) {
            return "arm_32";
        }
        if ("aarch64".equals(value)) {
            return "aarch_64";
        }
        if (value.matches("^(mips|mips32)$")) {
            return "mips_32";
        }
        if (value.matches("^(mipsel|mips32el)$")) {
            return "mipsel_32";
        }
        if ("mips64".equals(value)) {
            return "mips_64";
        }
        if ("mips64el".equals(value)) {
            return "mipsel_64";
        }
        if (value.matches("^(ppc|ppc32)$")) {
            return "ppc_32";
        }
        if (value.matches("^(ppcle|ppc32le)$")) {
            return "ppcle_32";
        }
        if ("ppc64".equals(value)) {
            return "ppc_64";
        }
        if ("ppc64le".equals(value)) {
            return "ppcle_64";
        }
        if ("s390".equals(value)) {
            return "s390_32";
        }
        if ("s390x".equals(value)) {
            return "s390_64";
        }

        return "unknown";
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
    }

    /**
     * 检测OS端口是否可以
     *
     * @param port 被检测的端口
     * @return true-可用，未被占用 false-端口被占用
     */
    public static boolean portAvailable(int port) {
        try {
            long process = PidUtil.findProcessByPort(port);
            if (process >= 0) {
                return false;
            }
        } catch (RejectedExecutionException e) {
            log.error(e.getMessage(), e);
            SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", port);
            try (Socket socket = new Socket()) {
                socket.connect(socketAddress, 50);
                return false;
            } catch (IOException ignored) {
            }
        }
        return true;
    }
}
