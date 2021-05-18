package org.beifengtz.jvmm.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Description: TODO 本类负责搜索物理机内可用的client，及向目标VM attach agent
 * </p>
 * <p>
 * Created in 10:10 上午 2021/5/16
 *
 * @author beifengtz
 */
public class WatchDog {
    private static final Logger log = LoggerFactory.getLogger(WatchDog.class);

    private static volatile WatchDog INSTANCE;

    private WatchDog() {
    }

    public static synchronized WatchDog getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WatchDog();
        }
        return INSTANCE;
    }

}
