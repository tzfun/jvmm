package org.beifengtz.jvmm.tools.util;

import java.util.Objects;
import java.util.UUID;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:30 2021/05/11
 *
 * @author beifengtz
 */
public class StringUtil {

    public static String genUUID(boolean upper) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        if (upper) {
            return uuid.toUpperCase();
        } else {
            return uuid.toLowerCase();
        }
    }

    public static String emptyOrDefault(String target, String def) {
        return Objects.isNull(target) || target.trim().isEmpty() ? def : target;
    }

    public static boolean isEmpty(String str) {
        return Objects.isNull(str) || str.isEmpty();
    }

    /**
     * 多个字符串都不为空
     *
     * @param strings 字符串数组
     */
    public static boolean nonNull(String... strings) {
        for (String obj : strings) {
            if (isEmpty(obj)) {
                return false;
            }
        }
        return true;
    }
}
