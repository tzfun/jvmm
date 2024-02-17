package org.beifengtz.jvmm.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:35 2021/05/11
 *
 * @author beifengtz
 */
public class CommonUtil {

    public static <K, V> HashMap<K, V> hasMapOf(K k1, V v1) {
        HashMap<K, V> map = new HashMap<>(5);
        map.put(k1, v1);
        return map;
    }

    public static <K, V> HashMap<K, V> hasMapOf(K k1, V v1, K k2, V v2) {
        HashMap<K, V> map = new HashMap<>(5);
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    public static <K, V> HashMap<K, V> hasMapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        HashMap<K, V> map = new HashMap<>(5);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }

    public static <K, V> HashMap<K, V> hasMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        HashMap<K, V> map = new HashMap<>(5);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        return map;
    }

    public static <K, V> HashMap<K, V> hasMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        HashMap<K, V> map = new HashMap<>(5);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        return map;
    }

    public static <T> HashSet<T> hashSetOf(T b) {
        HashSet<T> set = new HashSet<>();
        set.add(b);
        return set;
    }

    public static <T> HashSet<T> hashSetOf(T b, T b1) {
        HashSet<T> set = new HashSet<>();
        set.add(b);
        set.add(b1);
        return set;
    }

    public static <T> HashSet<T> hashSetOf(T b, T b1, T b2) {
        HashSet<T> set = new HashSet<>();
        set.add(b);
        set.add(b1);
        set.add(b2);
        return set;
    }

    public static <T> HashSet<T> hashSetOf(T b, T b1, T b2, T b3) {
        HashSet<T> set = new HashSet<>();
        set.add(b);
        set.add(b1);
        set.add(b2);
        set.add(b3);
        return set;
    }

    public static <T> HashSet<T> hashSetOf(T b, T b1, T b2, T b3, T b4) {
        HashSet<T> set = new HashSet<>();
        set.add(b);
        set.add(b1);
        set.add(b2);
        set.add(b3);
        set.add(b4);
        return set;
    }

    public static <T> HashSet<T> hashSetOf(T b, T b1, T b2, T b3, T b4, T b5) {
        HashSet<T> set = new HashSet<>();
        set.add(b);
        set.add(b1);
        set.add(b2);
        set.add(b3);
        set.add(b4);
        set.add(b5);
        return set;
    }

    public static <T> boolean arrayContains(T[] array, T target) {
        for (T t : array) {
            if (t.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public static <T> T nullOrDefault(T t, T def) {
        return Objects.isNull(t) ? def : t;
    }

    public static float between(float max, float min, float item) {
        if (item > max) {
            return max;
        }
        return Math.max(item, min);
    }

    /**
     * 多个对象都不为空
     *
     * @param objects 对象数组
     * @return true - if all objects is not null
     */
    public static boolean nonNull(Object... objects) {
        for (Object obj : objects) {
            if (Objects.isNull(obj)) {
                return false;
            }
        }
        return true;
    }

    public static void print(Object... objects) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < objects.length; ++i) {
            sb.append(objects[i]);
            if (i != objects.length - 1) {
                sb.append("\t");
            }
        }
        System.out.println(sb);
    }

    public static void printErr(Object... objects) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < objects.length; ++i) {
            sb.append(objects[i]);
            if (i != objects.length - 1) {
                sb.append("\t");
            }
        }
        System.err.println(sb);
    }

    public static <T> String join(String splitter, Collection<T> arr) {
        StringBuilder sb = new StringBuilder();
        for (T t : arr) {
            sb.append(t).append(splitter);
        }
        int len = sb.length();
        if (len > 1) {
            sb.delete(len - 1, len);
        }
        return sb.toString();
    }

    public static String getJvmmVersion() {
        InputStream is = CommonUtil.class.getResourceAsStream("/jvmm-version.txt");
        if (is != null) {
            try {
                return IOUtil.toString(is).trim();
            } catch (IOException ignored) {
            }
        }
        return "0.0.0";
    }
}
