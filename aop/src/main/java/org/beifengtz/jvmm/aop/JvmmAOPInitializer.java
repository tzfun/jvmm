package org.beifengtz.jvmm.aop;

import org.beifengtz.jvmm.aop.annotation.AspectJoin;
import org.beifengtz.jvmm.aop.core.transformer.ExecutorTransformer;
import org.beifengtz.jvmm.aop.core.transformer.ForkJoinTaskTransformer;
import org.beifengtz.jvmm.aop.listener.MethodListener;
import org.beifengtz.jvmm.aop.core.transformer.MethodTransformer;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * description: TODO
 * date: 15:35 2023/6/30
 *
 * @author beifengtz
 */
public class JvmmAOPInitializer {

    private static final Map<String, Object> instanceMap = new HashMap<>();
    private static volatile boolean ADDED_EXECUTOR_TRANSFORMER = false;

    /**
     * 初始化并扫描AOP相关注解
     *
     * @param packagePrefix   扫描包前缀，例如：org.beifengtz.jvmm
     * @param instrumentation {@link Instrumentation}实例
     * @throws Exception 初始化失败时抛出
     */
    public static synchronized void initAspect(String packagePrefix, Instrumentation instrumentation) throws Exception {
        Set<Class<?>> classes = scanAnnotation(packagePrefix, AspectJoin.class);

        for (Class<?> clazz : classes) {
            String className = clazz.getName();

            AspectJoin aj = clazz.getAnnotation(AspectJoin.class);
            Object o = instanceMap.get(className);
            if (o == null) {
                instanceMap.put(className, o = clazz.newInstance());
            }

            new MethodTransformer(
                    aj.classPattern(),
                    aj.classIgnorePattern().isEmpty() ? null : aj.classIgnorePattern(),
                    aj.methodPattern(),
                    aj.methodIgnorePattern().isEmpty() ? null : aj.methodIgnorePattern(),
                    (MethodListener) o
            ).enhance(instrumentation);
        }
    }

    /**
     * 初始化Tracing，其中会对线程池进行增强。
     * 注意！如果你的工程实现依赖于JDK的线程池，此方法应该在加载Executor之前调用。
     *
     * @param instrumentation {@link Instrumentation}
     */
    public static synchronized void initTracing(Instrumentation instrumentation) {
        if (ADDED_EXECUTOR_TRANSFORMER) {
            System.err.println("Jvmm tracing already initialized");
            return;
        }
        instrumentation.addTransformer(new ExecutorTransformer());
        instrumentation.addTransformer(new ForkJoinTaskTransformer());
        ADDED_EXECUTOR_TRANSFORMER = true;
    }

    /**
     * 从包package中获取所有的 Class，仅载入，不对其进行初始化
     *
     * @param pack 包地址
     * @return Class集合
     */
    private static Set<Class<?>> getClasses(String pack) {
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
                    findAndAddClassesInPackageByFile(packageName, filePath, classes);
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
     * @param classes     类集合，搜索后会存进此集合
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, Set<Class<?>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirFiles = dir.listFiles(file -> (file.isDirectory()) || (file.getName().endsWith(".class")));
        if (dirFiles == null) {
            return;
        }
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), classes);
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
    private static Set<Class<?>> scanAnnotation(String pack, Class<? extends Annotation> annotation) {
        Set<Class<?>> classes = getClasses(pack);

        Set<Class<?>> result = new HashSet<>();
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(annotation)) {
                result.add(clazz);
            }
        }
        return result;
    }
}
