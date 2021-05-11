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
public class CollectionFactory {

    private static JvmmCollector jvmmCollector = null;

    public static synchronized JvmmCollector getCollector() {
        if (jvmmCollector == null) {
            jvmmCollector = new DefaultJvmmCollector();
        }
        return jvmmCollector;
    }
}
