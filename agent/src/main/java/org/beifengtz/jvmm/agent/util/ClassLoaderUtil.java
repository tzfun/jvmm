package org.beifengtz.jvmm.agent.util;

import org.beifengtz.jvmm.agent.JvmmAgentClassLoader;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created in 14:58 2021/5/18
 *
 * @author beifengtz
 */
public class ClassLoaderUtil {

    public static ClassLoader systemLoadJar(URL jar) throws Throwable {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        if (cl instanceof URLClassLoader) {
            classLoaderAddURL((URLClassLoader) cl, jar);
            return cl;
        } else {
            throw new UnsupportedOperationException("Can not load jar library from " + cl.getClass());
        }
    }

    public static void classLoaderAddURL(URLClassLoader classLoader, URL url) throws Throwable {
        Method addURL = classLoader.getClass().getSuperclass().getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);
        addURL.invoke(classLoader, url);
    }

    public static void classLoaderAddURL(JvmmAgentClassLoader classLoader, URL url) {
        classLoader.addURL(url);
    }
}
