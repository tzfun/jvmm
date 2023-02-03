package org.beifengtz.jvmm.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
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
                Field field = classLoader.getClass().getDeclaredField("ucp");
                field.setAccessible(true);
                Object ucp = field.get(classLoader);

                Method method = ucp.getClass().getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);

                method.invoke(ucp, jar);
            } catch (Exception exception) {
                exception.printStackTrace();
                throw new IllegalStateException(exception.getMessage(), exception);
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
