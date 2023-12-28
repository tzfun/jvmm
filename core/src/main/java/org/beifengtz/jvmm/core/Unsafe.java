package org.beifengtz.jvmm.core;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.exception.ExecutionException;
import org.beifengtz.jvmm.core.entity.info.JvmClassLoaderInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Description: 此类中的方法是不安全的，无法保证方法是否能一定成功调用，也不能保证调用后会不会对程序造成影响，因此不建议使用者直接调用此类中的方法！
 * <p>
 * Created in 17:26 2022/9/23
 *
 * @author beifengtz
 */
public final class Unsafe {

    public interface TypeCode {
        String BOOLEAN = "Z";
        String BYTE = "B";
        String CHAR = "C";
        String SHORT = "S";
        String INT = "I";
        String LONG = "J";
        String FLOAT = "F";
        String DOUBLE = "D";
    }

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Unsafe.class);

    private static Method threadsMethod;
    private static Method findLoadedClassMethod;
    private static Field threadStatusFiled;

    static {
        try {
            //  sun.management.ThreadImpl#getThreads()
            Method method = Class.forName("sun.management.ThreadImpl").getDeclaredMethod("getThreads");
            method.setAccessible(true);
            threadsMethod = method;

            method = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            method.setAccessible(true);
            findLoadedClassMethod = method;

            threadStatusFiled = Thread.class.getDeclaredField("threadStatus");
            threadStatusFiled.setAccessible(true);
        } catch (Throwable e) {
            logger.error("Init Unsafe failed: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有线程对象
     *
     * @return Thread[]
     */
    public static Thread[] getThreads() {
        try {
            return (Thread[]) threadsMethod.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("Invoke Unsafe method 'getThreads()' error: " + e.getMessage(), e);
            return new Thread[0];
        }
    }

    /**
     * 根据线程id获取线程对象
     *
     * @param threadId 线程tid
     * @return Thread实例
     * @throws ExecutionException 调用异常
     */
    public static Thread getThread(long threadId) {
        for (Thread thread : getThreads()) {
            if (thread.getId() == threadId) {
                return thread;
            }
        }
        return null;
    }

    public static int getThreadNativeStatus(Thread thread) {
        try {
            return (int) threadStatusFiled.get(thread);
        } catch (IllegalAccessException e) {
            throw new ExecutionException(e);
        }
    }

    /**
     * 查看某一个类被哪些ClassLoader加载过
     *
     * @param className 类名
     * @return List of {@link ClassLoader}
     * @throws ExecutionException 调用异常
     */
    public static List<ClassLoader> findLoadedClassLoader(String className) throws Exception {
        List<ClassLoader> classLoaders = new ArrayList<>();
        Set<ClassLoader> scannedClassLoader = new HashSet<>();
        for (Thread thread : getThreads()) {
            ClassLoader classLoader = thread.getContextClassLoader();
            if (classLoader == null) {
                continue;
            }
            if (scannedClassLoader.add(classLoader)) {
                Class<?> clazz = (Class<?>) findLoadedClassMethod.invoke(classLoader, className);
                if (clazz != null) {
                    classLoaders.add(classLoader);
                }
            }
        }
        return classLoaders;
    }

    /**
     * 查找一个已经被加载过的Class对象
     *
     * @param className 类路径
     * @return Class对象列表
     * @throws ExecutionException 调用异常
     */
    public static List<Class<?>> findLoadedClasses(String className) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        Set<ClassLoader> scannedClassLoader = new HashSet<>();
        for (Thread thread : getThreads()) {
            ClassLoader classLoader = thread.getContextClassLoader();
            if (classLoader == null) {
                continue;
            }
            if (scannedClassLoader.add(classLoader)) {
                Class<?> clazz = (Class<?>) findLoadedClassMethod.invoke(classLoader, className);
                if (clazz != null) {
                    classes.add(clazz);
                }
            }
        }
        return classes;
    }

    /**
     * 判断一个ClassLoader是否已经加载了类
     *
     * @param classLoaderHash ClassLoader的hashCode
     * @param className       类路径
     * @return Class实例
     * @throws Exception 调用异常
     */
    public static Class<?> findLoadedClass(int classLoaderHash, String className) throws Exception {
        for (Thread thread : getThreads()) {
            ClassLoader classLoader = thread.getContextClassLoader();
            if (classLoader == null) {
                continue;
            }
            if (classLoader.hashCode() == classLoaderHash) {
                Class<?> clazz = (Class<?>) findLoadedClassMethod.invoke(classLoader, className);
                if (clazz != null) {
                    return clazz;
                }
            }
        }
        return null;
    }

    /**
     * 判断一个ClassLoader是否已经加载了类
     *
     * @param classLoader ClassLoader实例
     * @param className   类路径
     * @return 如果已加载返回Class对象，否则返回null
     * @throws Exception 调用失败
     */
    public static Class<?> findLoadedClass(ClassLoader classLoader, String className) throws Exception {
        return (Class<?>) findLoadedClassMethod.invoke(classLoader, className);
    }

    /**
     * 获取所有 ClassLoader信息
     *
     * @return list of {@link JvmClassLoaderInfo}
     */
    public static List<JvmClassLoaderInfo> getClassLoaders() {
        List<JvmClassLoaderInfo> list = new ArrayList<>();
        Set<ClassLoader> scannedClassLoader = new HashSet<>();
        for (Thread thread : getThreads()) {
            ClassLoader classLoader = thread.getContextClassLoader();
            if (classLoader == null) {
                continue;
            }
            if (scannedClassLoader.add(classLoader)) {
                JvmClassLoaderInfo info = JvmClassLoaderInfo.create(classLoader.hashCode()).setName(classLoader.getClass().getName());

                ClassLoader parent = classLoader.getParent();
                while (parent != null) {
                    info.addParents(parent.getClass().getName());
                    parent = parent.getParent();
                }

                list.add(info);
            }
        }
        return list;
    }

    /**
     * 根据 hashcode 查找对应的 ClassLoader
     *
     * @param hash ClassLoader的 hashcode
     * @return {@link ClassLoader}实例
     */
    public static ClassLoader getClassLoader(int hash) {
        if (hash == 0) {
            return null;
        }
        for (Thread thread : getThreads()) {
            ClassLoader classLoader = thread.getContextClassLoader();
            if (classLoader == null) {
                continue;
            }
            if (classLoader.hashCode() == hash) {
                return classLoader;
            }
        }
        return null;
    }
}
