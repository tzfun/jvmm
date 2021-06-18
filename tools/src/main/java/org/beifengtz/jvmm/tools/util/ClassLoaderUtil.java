package org.beifengtz.jvmm.tools.util;

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

    public static ClassLoader systemLoadJar(URL jar) throws Throwable {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

        Method addURL = systemClassLoader.getClass().getSuperclass().getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);
        addURL.invoke(systemClassLoader, jar);
        return systemClassLoader;
    }
}