package org.beifengtz.jvmm.tools.util;

import org.beifengtz.jvmm.tools.PlatformEnum;

import java.util.Locale;

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
    private static final String OPERATING_SYSTEM_NAME = SystemPropertyUtil.get("os.name").toLowerCase(Locale.ENGLISH);
    private static final String OPERATING_SYSTEM_ARCH = SystemPropertyUtil.get("os.arch").toLowerCase(Locale.ENGLISH);

    private static PlatformEnum platform;

    private static String arch;

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
}
