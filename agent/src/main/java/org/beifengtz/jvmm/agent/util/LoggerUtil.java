package org.beifengtz.jvmm.agent.util;

import org.beifengtz.jvmm.agent.DefaultImplLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: TODO
 * date: 14:41 2023/4/28
 *
 * @author beifengtz
 */
public class LoggerUtil {

    private enum LoggerMethod {
        trace_str,
        trace_str_t,
        trace_str_arg,
        info_str,
        info_str_t,
        info_str_arg,
        warn_str,
        warn_str_t,
        warn_str_arg,
        debug_str,
        debug_str_t,
        debug_str_arg,
        error_str,
        error_str_t,
        error_str_arg,
    }

    private static volatile boolean LOADED_LOGGER = false;
    private static boolean CONTAINS_LOGGER_FRAME = false;
    private static final DefaultImplLogger DEFAULT_LOGGER = new DefaultImplLogger();
    private static Method FRAME_GET_LOGGER_METHOD = null;
    private static Map<LoggerMethod, Method> methodCache = null;

    static {
        init();
    }

    public static void init() {
        loggerDefault("info", "Initializing logger...");
        if (!LOADED_LOGGER) {
            synchronized (LoggerUtil.class) {
                if (!LOADED_LOGGER) {
                    try {
                        Class<?> clazz = Class.forName("io.netty.util.internal.logging.InternalLoggerFactory");

                        Method newDefaultFactory = clazz.getDeclaredMethod("newDefaultFactory", String.class);
                        newDefaultFactory.setAccessible(true);

                        Object factory = newDefaultFactory.invoke(null, "jvmm");
                        //  没有默认的logger框架，尝试设置 JvmmLogger
                        if (factory.getClass().getName().equals("io.netty.util.internal.logging.JdkLoggerFactory")) {
                            Method setDefaultFactory = clazz.getMethod("setDefaultFactory", clazz);
                            Class<?> jvmmLoggerFactoryClass = Class.forName("org.beifengtz.jvmm.log.JvmmLoggerFactory");
                            setDefaultFactory.invoke(null, jvmmLoggerFactoryClass.getMethod("getInstance").invoke(null));
                        }

                        FRAME_GET_LOGGER_METHOD = clazz.getMethod("getInstance", String.class);
                        methodCache = new ConcurrentHashMap<>();
                        CONTAINS_LOGGER_FRAME = true;
                        loggerDefault("info", "Loaded default logging framework");
                    } catch (Exception e) {
                        loggerDefault("info", "No logging framework found, jvmm logger will be used");
                    }
                }
            }
            LOADED_LOGGER = true;
        }
    }

    public static void logger(String name, String methodName, String message) {
        if (CONTAINS_LOGGER_FRAME) {
            try {
                Object logger = FRAME_GET_LOGGER_METHOD.invoke(null, name);
                Method method = methodCache.computeIfAbsent(LoggerMethod.valueOf(methodName + "_str"), o -> {
                    try {
                        return logger.getClass().getDeclaredMethod(methodName, String.class);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
                method.invoke(logger, message);
            } catch (IllegalAccessException | InvocationTargetException e) {
                loggerDefault("error", "Internal logger invoke error", e);
            }
        } else {
            loggerDefault(methodName, message);
        }
    }

    public static void logger(String name, String methodName, String message, Throwable throwable) {
        if (CONTAINS_LOGGER_FRAME) {
            try {
                Object logger = FRAME_GET_LOGGER_METHOD.invoke(null, name);
                Method method = methodCache.computeIfAbsent(LoggerMethod.valueOf(methodName + "_str_t"), o -> {
                    try {
                        return logger.getClass().getDeclaredMethod(methodName, String.class, Throwable.class);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
                method.invoke(logger, message, throwable);
            } catch (IllegalAccessException | InvocationTargetException e) {
                loggerDefault("error", "Internal logger invoke error", e);
            }
        } else {
            loggerDefault(methodName, message, throwable);
        }
    }

    public static void logger(String name, String methodName, String message, Object... args) {
        if (CONTAINS_LOGGER_FRAME) {
            try {
                Object logger = FRAME_GET_LOGGER_METHOD.invoke(null, name);
                Method method = methodCache.computeIfAbsent(LoggerMethod.valueOf(methodName + "_str_arg"), o -> {
                    try {
                        return logger.getClass().getDeclaredMethod(methodName, String.class, Object[].class);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
                method.invoke(logger, message, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                loggerDefault("error", "Internal logger invoke error", e);
            }
        } else {
            loggerDefault(methodName, message, args);
        }
    }

    private static void loggerDefault(String methodName, String message) {
        loggerDefault(methodName, message, (Throwable) null);
    }

    private static void loggerDefault(String methodName, String message, Throwable t) {
        switch (methodName) {
            case "trace":
                DEFAULT_LOGGER.trace(message, t);
                break;
            case "info":
                DEFAULT_LOGGER.info(message, t);
                break;
            case "warn":
                DEFAULT_LOGGER.warn(message, t);
                break;
            case "debug":
                DEFAULT_LOGGER.debug(message, t);
                break;
            case "error":
                DEFAULT_LOGGER.error(message, t);
                break;
        }
    }

    private static void loggerDefault(String methodName, String message, Object... args) {
        switch (methodName) {
            case "trace":
                DEFAULT_LOGGER.trace(message, args);
                break;
            case "info":
                DEFAULT_LOGGER.info(message, args);
                break;
            case "warn":
                DEFAULT_LOGGER.warn(message, args);
                break;
            case "debug":
                DEFAULT_LOGGER.debug(message, args);
                break;
            case "error":
                DEFAULT_LOGGER.error(message, args);
                break;
        }
    }

    public static void info(Class<?> clazz, String message) {
        logger(clazz.getName(), "info", message);
    }

    public static void warn(Class<?> clazz, String message) {
        logger(clazz.getName(), "warn", message);
    }

    public static void error(Class<?> clazz, String message, Throwable throwable) {
        logger(clazz.getName(), "error", message, throwable);
    }

    public static void error(Class<?> clazz, String message) {
        logger(clazz.getName(), "error", message);
    }

    public static void debug(Class<?> clazz, String message) {
        logger(clazz.getName(), "debug", message);
    }
}
