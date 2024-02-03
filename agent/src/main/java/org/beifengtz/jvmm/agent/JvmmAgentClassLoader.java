package org.beifengtz.jvmm.agent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Created in 16:34 2021/5/22
 *
 * @author beifengtz
 */
public class JvmmAgentClassLoader extends URLClassLoader {

    public JvmmAgentClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public JvmmAgentClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader().getParent());
    }

    @Override
    public URL getResource(String name) {
        URL resource = getParent().getResource(name);
        if (resource != null) {
            return resource;
        }
        ClassLoader cl = getClass().getClassLoader();
        if (cl != null) {
            resource = cl.getResource(name);
            if (resource != null) {
                return resource;
            }
        }
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> resources = getParent().getResources(name);
        if (resources.hasMoreElements()) {
            return resources;
        }
        ClassLoader cl = getClass().getClassLoader();
        if (cl != null) {
            resources = cl.getResources(name);
            if (resources.hasMoreElements()) {
                return resources;
            }
        }
        return super.getResources(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.loadClass(name, false);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        if (name != null && (name.startsWith("sun.") || name.startsWith("java."))) {
            return super.loadClass(name, resolve);
        }

        try {
            Class<?> clazz = findClass(name);
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        } catch (Exception ignored) {
        }
        //  slf4j优先从其他环境中搜索
        if (name != null && name.startsWith("org.slf4j")) {
            try {
                return super.loadClass(name, resolve);
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                return searchFromOtherClassLoader(name);
            }
        } else {
            return super.loadClass(name, resolve);
        }
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    private Class<?> searchFromOtherClassLoader(String name) throws ClassNotFoundException {
        try {
            Method method = Class.forName("sun.management.ThreadImpl").getDeclaredMethod("getThreads");
            method.setAccessible(true);
            Thread[] threads = (Thread[]) method.invoke(null);
            Set<ClassLoader> scannedClassLoader = new HashSet<>();
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            for (Thread thread : threads) {
                ClassLoader classLoader = thread.getContextClassLoader();
                if (classLoader == null || classLoader == this) {
                    continue;
                }
                //  只找app classloader的子节点
                if (classLoader == systemClassLoader || classLoader.getParent() == systemClassLoader && scannedClassLoader.add(classLoader)) {
                    try {
                        return classLoader.loadClass(name);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new ClassNotFoundException("Class " + name + " not found", e);
        }
        throw new ClassNotFoundException("Class " + name + " not found");
    }
}
