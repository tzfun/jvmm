package org.beifengtz.jvmm.core.ext.os;

import org.beifengtz.jvmm.common.PlatformEnum;
import org.beifengtz.jvmm.common.exception.OsNotSupportedException;
import org.beifengtz.jvmm.common.util.PlatformUtil;
import org.beifengtz.jvmm.core.entity.result.OsNetIOResult;
import org.beifengtz.jvmm.core.entity.result.OsNetStateResult;

/**
 * Description: TODO
 *
 * Created in 11:27 2022/9/28
 *
 * @author beifengtz
 */
public interface OsProvider {

    static OsProvider get() {
        if (PlatformUtil.isLinux()) {
            return LinuxProvider.getInstance();
        } else if (PlatformUtil.isWindows()) {
            return WindowsProvider.getInstance();
        } else if (PlatformUtil.isMac()) {
            return MacProvider.getInstance();
        } else {
            throw new OsNotSupportedException(PlatformUtil.getOS().name());
        }
    }

    default PlatformEnum platform() {
        return PlatformUtil.getOS();
    }

    default String arch() {
        return PlatformUtil.arch();
    }

    /**
     * 获取网络IO统计
     *
     * @return {@link OsNetIOResult}
     */
    OsNetIOResult getNetIO();

    /**
     * 获取Tcp连接状态
     *
     * @return {@link OsNetStateResult}
     */
    OsNetStateResult getTcpState();
}
