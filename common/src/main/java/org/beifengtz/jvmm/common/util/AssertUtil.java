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
        checkArguments(condition,"Invalid argument");
    }

    public static void checkArguments(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
