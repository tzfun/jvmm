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
            return LinuxProvider.INSTANCE;
        } else if (PlatformUtil.isWindows()) {
            return WindowsProvider.INSTANCE;
        } else if (PlatformUtil.isMac()) {
            return MacProvider.INSTANCE;
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
     * 开启异步执行线程，如果你的查询比较频繁，建议开启此线程
     * <p></p>
     * 因为有些操作在特定的操作系统环境下，同步执行是秒级响应的，如果开启了定期执行线程所有查询接口都是异步的，响应是微妙级甚至纳秒级别的。
     */
    void start();

    /**
     * 关闭异步执行线程
     */
    void shutdown();

    /**
     * 异步线程是否在运行状态
     * @return true-线程正在运行中
     */
    boolean isRunning();

    /**
     * 设置异步线程每个任务之间的间隔时间
     * @param secs 间隔时间，单位秒，一定大于{@link OsScheduledService#MIN_SCHEDULE_SECS}
     */
    void setIntervalSecs(int secs);

    /**
     * 获取间隔异步线程每个任务之间的间隔时间
     * @return 间隔时间，单位秒，一定大于{@link OsScheduledService#MIN_SCHEDULE_SECS}
     */
    int getIntervalSecs();

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
