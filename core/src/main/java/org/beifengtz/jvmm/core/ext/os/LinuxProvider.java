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
class LinuxProvider extends OsScheduledService implements OsProvider {
    static final LinuxProvider INSTANCE = new LinuxProvider().start();
    private LinuxProvider(){}

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
