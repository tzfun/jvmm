package org.beifengtz.jvmm.core;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 17:17 2021/5/11
 *
 * @author beifengtz
 */
public interface JvmmScheduleService {

    JvmmScheduleService setTask(Runnable task);

    /**
     * 设置执行周期数
     *
     * @param times 周期数，小于等于0时表示无限循环，默认-1
     */
    JvmmScheduleService setTimes(int times);

    /**
     * 更新任务周期执行间隙
     *
     * @param gapSeconds 秒为单位的时间间隙
     * @return 更新后的时间间隙，秒为单位
     */
    JvmmScheduleService setTimeGap(int gapSeconds);

    /**
     * @param stopOnError true-遇到异常时停止循环 ，false-忽略异常，默认值是true
     */
    JvmmScheduleService setStopOnError(boolean stopOnError);

    void start();

    /**
     * 服务关闭钩子函数
     */
    void stop();

}
