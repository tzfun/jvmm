package org.beifengtz.jvmm.common.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 17:21 2021/5/30
 *
 * @author beifengtz
 */
public class ReflexUtil {

    /**
     * 从包package中获取所有的 Class，仅载入，不对其进行初始化
     *
     * @param pack      包地址
     * @param recursive 是否递归搜索，例如包下面还有子包，如果为true则递归搜索子包下的所有class，如果为false仅搜索当前目录下的class，不对子包搜索
     * @return Class集合
     */
    public static Set<Class<?>> getClasses(String pack, boolean recursive) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.charAt(0) == '/') {
                                name = name.substring(1);
                            }
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                if (idx != -1) {
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                if (name.endsWith(".class") && !entry.isDirectory()) {
                                    String className = name.substring(packageName.length() + 1, name.length() - 6);
                                    try {
                                        classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName 包名
     * @param packagePath 包路径
     * @param recursive   是否递归搜索
     * @param classes     类集合，搜索后会存进此集合
     */
    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive,
                                                        Set<Class<?>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirFiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
        if (dirFiles == null) {
            return;
        }
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(),
                        recursive, classes);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 扫描某个包下注有某个注解的类
     *
     * @param pack       包路径：例如 org.beifengtz.jvmm
     * @param annotation 被扫描的注解类
     * @return {@link Class}集合
     */
    public static Set<Class<?>> scanAnnotation(String pack, Class<? extends Annotation> annotation) {
        Set<Class<?>> classes = getClasses(pack, true);

        Set<Class<?>> result = new HashSet<>();
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(annotation)) {
                result.add(clazz);
            }
        }
        return result;
    }

    /**
     * 扫描某个包下所有类中注有某个注解的方法
     *
     * @param pack       包路径：例如 org.beifengtz.jvmm
     * @param annotation 被扫描的注解类
     * @return {@link Method}集合
     */
    public static Set<Method> scanMethodAnnotation(String pack, Class<? extends Annotation> annotation) {
        return scanMethodAnnotation(getClasses(pack, true), annotation);
    }

    /**
     * 从某个类中扫描被注解有 annotation 的方法
     *
     * @param clazz      类
     * @param annotation 注解类
     * @return 方法集合
     */
    public static Set<Method> scanMethodAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        return scanMethodAnnotation(CommonUtil.hashSetOf(clazz), annotation);
    }

    /**
     * 扫描类集合下注解有某个annotation的方法
     *
     * @param classes    被扫描的类集合
     * @param annotation 被扫描的注解类
     * @return {@link Method}集合
     */
    public static Set<Method> scanMethodAnnotation(Set<Class<?>> classes, Class<? extends Annotation> annotation) {
        Set<Method> result = new HashSet<>();
        for (Class<?> clazz : classes) {
            Method[] methods = clazz.getMethods();
            for (Method m : methods) {
                if (m.isAnnotationPresent(annotation)) {
                    result.add(m);
                }
            }
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object parseValueFromStr(Class<?> type, String source) {
        if (source == null) {
            return null;
        }
        if (type.isAssignableFrom(String.class)) {
            return source;
        } else if (type.isAssignableFrom(byte.class)) {
            return Byte.parseByte(source);
        } else if (type.isAssignableFrom(Byte.class)) {
            return Byte.valueOf(source);
        } else if (type.isAssignableFrom(short.class)) {
            return Short.parseShort(source);
        } else if (type.isAssignableFrom(Short.class)) {
            return Short.valueOf(source);
        } else if (type.isAssignableFrom(int.class)) {
            return Integer.parseInt(source);
        } else if (type.isAssignableFrom(Integer.class)) {
            return Integer.valueOf(source);
        } else if (type.isAssignableFrom(float.class)) {
            return Float.parseFloat(source);
        } else if (type.isAssignableFrom(Float.class)) {
            return Float.valueOf(source);
        } else if (type.isAssignableFrom(double.class)) {
            return Double.parseDouble(source);
        } else if (type.isAssignableFrom(Double.class)) {
            return Double.valueOf(source);
        } else if (type.isAssignableFrom(boolean.class)) {
            return Boolean.parseBoolean(source);
        } else if (type.isAssignableFrom(Boolean.class)) {
            return Boolean.valueOf(source);
        } else if (type.isAssignableFrom(long.class)) {
            return Long.parseLong(source);
        } else if (type.isAssignableFrom(Long.class)) {
            return Long.valueOf(source);
        } else if (type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) type, source);
        }
        return null;
    }

    /**
     * 是否是基础数据类型，包含装箱类
     *
     * @param type 目标类型
     * @return true or false
     */
    public static boolean isBaseType(Class<?> type) {
        if (type == null) {
            return false;
        }
        return type.isAssignableFrom(byte.class) || type.isAssignableFrom(Byte.class)
                || type.isAssignableFrom(short.class) || type.isAssignableFrom(Short.class)
                || type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)
                || type.isAssignableFrom(float.class) || type.isAssignableFrom(Float.class)
                || type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)
                || type.isAssignableFrom(boolean.class) || type.isAssignableFrom(Boolean.class)
                || type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class);
    }

    /**
     * 获取基础数据类型的默认值
     *
     * @param type 目标类型
     * @return 默认值
     */
    public static Object getBaseTypeDefault(Class<?> type) {
        if (type == null) {
            return null;
        }
        if (type.isAssignableFrom(byte.class)
                || type.isAssignableFrom(short.class)
                || type.isAssignableFrom(int.class)
                || type.isAssignableFrom(float.class)
                || type.isAssignableFrom(double.class)
                || type.isAssignableFrom(boolean.class)
                || type.isAssignableFrom(long.class)) {
            return 0;
        }
        return null;
    }
}
