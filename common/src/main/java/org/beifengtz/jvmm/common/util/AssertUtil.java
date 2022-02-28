package org.beifengtz.jvmm.common.util;

/**
 * Description: TODO
 *
 * Created in 11:39 2022/2/25
 *
 * @author beifengtz
 */
public class AssertUtil {

    public static void checkArguments(boolean condition) {
        checkArguments(condition, "Invalid argument");
    }

    public static void checkArguments(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <T> T notNull(T target, String format, Object... args) {
        if (target == null) {
            throw new NullPointerException(String.format(format, args));
        }
        return target;
    }

    public static <T> T[] notEmpty(final T[] array, final String message, final Object... values) {
        if (array == null) {
            throw new NullPointerException(String.format(message, values));
        }
        if (array.length == 0) {
            throw new IllegalArgumentException(String.format(message, values));
        }
        return array;
    }
}
