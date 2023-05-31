package org.beifengtz.jvmm.agent.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created in 14:58 2021/5/18
 *
 * @author beifengtz
 */
public class ClassLoaderUtil {

    public static void loadJar(ClassLoader classLoader, URL jar) throws Throwable {
        if (classLoader instanceof URLClassLoader) {
            urlClassloaderLoadJar((URLClassLoader) classLoader, jar);
        } else {
            try {
                Method method = classLoader.getClass().getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
                method.setAccessible(true);
                method.invoke(classLoader, jar.getFile());
            } catch (Throwable e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    public static ClassLoader systemLoadJar(URL jar) throws Throwable {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        loadJar(classLoader, jar);
        return classLoader;
    }

    private static void urlClassloaderLoadJar(URLClassLoader classLoader, URL jar) throws Throwable {
        Method addURL = classLoader.getClass().getSuperclass().getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);
        addURL.invoke(classLoader, jar);
    }
}
