package org.beifengtz.jvmm.tools;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:34 2021/5/22
 *
 * @author beifengtz
 */
public class JvmmClassLoader extends URLClassLoader {
    public JvmmClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader().getParent());
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.loadClass(name,true);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null){
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

        return super.loadClass(name, resolve);
    }
}
