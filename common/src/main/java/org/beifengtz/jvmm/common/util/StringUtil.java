package org.beifengtz.jvmm.common.util;

import java.util.Collection;
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
        return Objects.isNull(str) || str.trim().isEmpty();
    }

    public static boolean nonEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 多个字符串都不为空
     *
     * @param strings 字符串数组
     * @return true 如果所有字符串都不为逻辑意义上的空
     */
    public static boolean nonNull(String... strings) {
        for (String obj : strings) {
            if (isEmpty(obj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将JSON字符串格式化
     *
     * @param json 未格式化的JSON字符串。
     * @return 格式化的JSON字符串。
     */
    public static String formatJsonCode(String json) {
        StringBuilder result = new StringBuilder();

        int length = json.length();
        int number = 0;
        char key = 0;
        for (int i = 0; i < length; i++) {
            key = json.charAt(i);

            if ((key == '[') || (key == '{')) {
                if ((i - 1 > 0) && (json.charAt(i - 1) == ':')) {
                    result.append(" ");
                }

                result.append(key);
                result.append('\n');

                number++;
                result.append(indent(number));
                continue;
            }

            if ((key == ']') || (key == '}')) {
                result.append('\n');

                number--;
                result.append(indent(number));

                result.append(key);
                continue;
            }

            if ((key == ',')) {
                result.append(key);
                result.append('\n');
                result.append(indent(number));
                continue;
            }

            result.append(key);
        }

        return result.toString();
    }

    /**
     * 返回指定次数的缩进字符串。每一次缩进三个空格，即SPACE。
     *
     * @param number 缩进次数。
     * @return 指定缩进次数的字符串。
     */
    private static String indent(int number) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < number; i++) {
            result.append("   ");
        }
        return result.toString();
    }

    @SafeVarargs
    public static <T> String join(String splitter, T... arr) {
        if (arr == null || arr.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) {
                sb.append(splitter);
            }
        }
        return sb.toString();
    }

    public static String join(String splitter, Collection<?> arr) {
        if (arr == null || arr.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object o : arr) {
            sb.append(o).append(splitter);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static String repeat(String item, int num) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < num; i++) {
            str.append(item);
        }
        return str.toString();
    }
}
