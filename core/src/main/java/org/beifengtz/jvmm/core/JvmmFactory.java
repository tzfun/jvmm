package org.beifengtz.jvmm.core;

import com.google.gson.Gson;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:32 2021/5/11
 *
 * @author beifengtz
 */
public class JvmmFactory {

    private static volatile JvmmCollector jvmmCollector = null;
    private static volatile JvmmExecutor jvmmExecutor = null;

    public static synchronized JvmmCollector getCollector() {
        if (jvmmCollector == null) {
            jvmmCollector = new DefaultJvmmCollector();
        }
        return jvmmCollector;
    }

    public static synchronized JvmmExecutor getExecutor() {
        if (jvmmExecutor == null) {
            jvmmExecutor = new DefaultJvmmExecutor();
        }
        return jvmmExecutor;
    }

    public static <T> T getEntity(String json, Class<T> clazz) {
        return new Gson().fromJson(json, clazz);
    }
}
