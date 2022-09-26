package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.core.entity.mx.ClassLoaderInfo;
import org.slf4j.Logger;

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

    private static final Logger logger = LoggerFactory.logger(Unsafe.class);

    private static Method threadsMethod;
    private static Method findLoadedClassMethod;

    static {
        try {
            //  sun.management.ThreadImpl#getThreads()
            Method method = Class.forName("sun.management.ThreadImpl").getDeclaredMethod("getThreads");
            method.setAccessible(true);
            threadsMethod = method;

            method = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            method.setAccessible(true);
            findLoadedClassMethod = method;
        } catch (Throwable e) {
            logger.error("Init Unsafe failed: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有线程对象
     *
     * @return Thread[]
     * @throws Exception 调用异常
     */
    public static Thread[] getThreads() throws Exception {
        return (Thread[]) threadsMethod.invoke(null);
    }

    /**
     * 根据线程id获取线程对象
     *
     * @param threadId 线程tid
     * @return Thread实例
     * @throws Exception 调用异常
     */
    public static Thread getThread(int threadId) throws Exception {
        for (Thread thread : getThreads()) {
            if (thread.getId() == threadId) {
                return thread;
            }
        }
        return null;
    }

    /**
     * 查看某一个类被哪些ClassLoader加载过
     *
     * @param className 类名
     * @return List of {@link ClassLoader}
     * @throws Exception 调用异常
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
     * @throws Exception 调用异常
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
     * @param classLoaderHash   ClassLoader的hashCode
     * @param className         类路径
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
     * @param classLoader  ClassLoader实例
     * @param className    类路径
     * @return 如果已加载返回Class对象，否则返回null
     */
    public static Class<?> findLoadedClass(ClassLoader classLoader, String className) throws Exception {
        return (Class<?>) findLoadedClassMethod.invoke(classLoader, className);
    }

    /**
     * 获取所有 ClassLoader信息
     *
     * @return list of {@link ClassLoaderInfo}
     * @throws Exception 调用失败
     */
    public static List<ClassLoaderInfo> getClassLoaders() throws Exception {
        List<ClassLoaderInfo> list = new ArrayList<>();
        Set<ClassLoader> scannedClassLoader = new HashSet<>();
        for (Thread thread : getThreads()) {
            ClassLoader classLoader = thread.getContextClassLoader();
            if (classLoader == null) {
                continue;
            }
            if (scannedClassLoader.add(classLoader)) {
                ClassLoaderInfo info = ClassLoaderInfo.create(classLoader.hashCode()).setName(classLoader.getClass().getName());

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

}
