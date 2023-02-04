package org.beifengtz.jvmm.core;

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
    private static volatile JvmmProfiler jvmmProfiler = null;

    public static JvmmCollector getCollector() {
        if (jvmmCollector == null) {
            synchronized (JvmmFactory.class) {
                if (jvmmCollector == null) {
                    jvmmCollector = new DefaultJvmmCollector();
                }
                return jvmmCollector;
            }
        }
        return jvmmCollector;
    }

    public static JvmmExecutor getExecutor() {
        if (jvmmExecutor == null) {
            synchronized (JvmmFactory.class) {
                if (jvmmExecutor == null) {
                    jvmmExecutor = new DefaultJvmmExecutor();
                }
                return jvmmExecutor;
            }
        }
        return jvmmExecutor;
    }

    public static JvmmProfiler getProfiler() {
        if (jvmmProfiler == null) {
            synchronized (JvmmFactory.class) {
                if (jvmmProfiler == null) {
                    jvmmProfiler = new DefaultJvmmProfiler();
                }
                return jvmmProfiler;
            }
        }
        return jvmmProfiler;
    }
}
