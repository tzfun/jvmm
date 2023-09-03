package org.beifengtz.jvmm.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:32 2021/05/11
 *
 * @author beifengtz
 */
public class SystemPropertyUtil {

    public static final String PROPERTY_JVMM_WORK_THREAD = "jvmm.workThread";
    public static final String PROPERTY_JVMM_PROFILER_LOADED = "jvmm.profiler.loaded";
    public static final String PROPERTY_JVMM_SCAN_PACKAGE = "jvmm.scanPackage";
    public static final String PROPERTY_JVMM_HOME = "jvmm.home";
    public static final String PROPERTY_JVMM_TEMP_PATH = "jvmm.tempPath";
    public static final String PROPERTY_JVMM_LOG_LEVEL = "jvmm.log.level";
    public static final String PROPERTY_JVMM_LOG_LEVELS = "jvmm.log.levels";
    public static final String PROPERTY_JVMM_LOG_FILE = "jvmm.log.file";
    public static final String PROPERTY_JVMM_LOG_FILE_NAME = "jvmm.log.fileName";
    public static final String PROPERTY_JVMM_LOG_FILE_LIMIT_SIZE = "jvmm.log.fileLimitSize";
    public static final String PROPERTY_JVMM_LOG_PATTERN = "jvmm.log.pattern";
    public static final String PROPERTY_JVMM_LOG_PRINTERS = "jvmm.log.printers";


    private static Logger logger() {
        return LoggerFactory.getLogger(SystemPropertyUtil.class);
    }

    public static boolean contains(String key) {
        return get(key) != null;
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(final String key, String def) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key must not be empty.");
        }

        String value = null;
        try {
            if (System.getSecurityManager() == null) {
                value = System.getProperty(key);
            } else {
                value = AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(key));
            }
        } catch (SecurityException e) {
            logger().warn("Unable to retrieve a system property '{}'; default values will be used.", key, e);
        }

        if (value == null) {
            return def;
        }

        return value;
    }

    public static boolean getBoolean(String key, boolean def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim().toLowerCase();
        if (value.isEmpty()) {
            return def;
        }

        if ("true".equals(value) || "yes".equals(value) || "1".equals(value)) {
            return true;
        }

        if ("false".equals(value) || "no".equals(value) || "0".equals(value)) {
            return false;
        }

        logger().warn(
                "Unable to parse the boolean system property '{}':{} - using the default value: {}",
                key, value, def
        );

        return def;
    }

    public static int getInt(String key, int def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim();
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            // Ignore
        }

        logger().warn(
                "Unable to parse the integer system property '{}':{} - using the default value: {}",
                key, value, def
        );

        return def;
    }

    public static long getLong(String key, long def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim();
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            // Ignore
        }

        logger().warn(
                "Unable to parse the long integer system property '{}':{} - using the default value: {}",
                key, value, def
        );

        return def;
    }
}
