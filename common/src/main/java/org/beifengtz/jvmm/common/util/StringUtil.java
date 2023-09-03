package org.beifengtz.jvmm.common.util;

import com.google.gson.Gson;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern emojiPattern = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]");
    private static final Gson gson = new Gson();

    public static Gson getGson() {
        return gson;
    }

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


    /**
     * 过滤掉emoji
     *
     * @param str 字符源
     * @return 过滤后的字符串
     */
    public static String filterEmoji(String str) {
        if (str == null || str.trim().isEmpty()) {
            return str;
        }
        return emojiPattern.matcher(str).replaceAll("");
    }

    /**
     * 判断是否包含emoji
     *
     * @param str 字符源
     * @return true or false
     */
    public static boolean containsEmoji(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        return emojiPattern.matcher(str).find();
    }

    /**
     * 优雅地格式化字节数
     *
     * @param bytes 字节数
     * @return 可视化的字节表示，比如：12.23MB, 1.7GB, 30KB, 120B
     */
    public static String formatByteSizeGracefully(long bytes) {
        double value = 0;
        String unit = "";
        if (bytes < 1024) {
            value = bytes;
            unit = "B";
        } else if (bytes < 1024 * 1024) {
            value = bytes / 1024.0;
            unit = "KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            value = bytes / (1024 * 1024.0);
            unit = "MB";
        } else {
            value = bytes / (1024 * 1024 * 1024.0);
            unit = "GB";
        }

        return BigDecimal.valueOf(value).setScale(2, RoundingMode.FLOOR).doubleValue() + unit;
    }

    /**
     * 驼峰转下划线
     *
     * @param str 需转换的驼峰字符串
     * @return 下划线字符串
     */
    public static String hump2Line(String str) {
        String s = str.replaceAll("[A-Z]", "_$0").toLowerCase();
        if (s.startsWith("_")) {
            return s.substring(1);
        }
        return s;
    }

    /**
     * 下划线转驼峰
     *
     * @param str 需转换的下划线字符串
     * @return 驼峰字符串
     */
    public static String line2Hump(String str) {
        Pattern linePattern = Pattern.compile("_(\\w)");

        str = str.toLowerCase();

        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return upperCaseFirst(sb.toString());
    }

    /**
     * 首字母转为大写
     *
     * @param s 字符源
     * @return result
     */
    public static String upperCaseFirst(String s) {
        if (Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
