package org.beifengtz.jvmm.core.ext.os;

import org.beifengtz.jvmm.core.entity.result.OsNetIOResult;
import org.beifengtz.jvmm.core.entity.result.OsNetStateResult;

/**
 * Description: TODO
 *
 * Created in 11:12 2022/9/29
 *
 * @author beifengtz
 */
class MacProvider extends OsScheduledService implements OsProvider {
    private static MacProvider INSTANCE;

    private MacProvider() {
    }

    public synchronized static MacProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MacProvider();
            INSTANCE.start();
        }
        return INSTANCE;
    }

    @Override
    public OsNetIOResult getNetIO() {
        return null;
    }

    @Override
    public OsNetStateResult getTcpState() {
        return null;
    }

    @Override
    public void run() {

    }
}
