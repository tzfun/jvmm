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
                if (e.getClass().getName().equals("java.lang.reflect.InaccessibleObjectException")) {
                    System.err.println("Unable to obtain reflection permissions. In a higher version of jdk, " +
                            "please add JVM parameters to enable related permissions: \n" +
                            "\t1. --add-opens java.base/jdk.internal.loader=ALL-UNNAMED\n" +
                            "\t2. --add-opens jdk.zipfs/jdk.nio.zipfs=ALL-UNNAMED\n" +
                            "\t3. --add-opens java.management/sun.management=ALL-UNNAMED\n" +
                            "See https://github.com/tzfun/jvmm#%E5%90%AF%E5%8A%A8jvmm%E6%97%B6%E6%8A%A5%E9%94%99-javalangillegalargumentexception-can-not-found-java-program-jps for more information.");
                }
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
