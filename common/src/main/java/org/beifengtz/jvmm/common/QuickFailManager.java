package org.beifengtz.jvmm.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: 快失败机制管理器，当某一个接口连续多次失败时，触发快失败，并不会真正地去执行，经过一定时间后再释放快失败，可避免线程大批量阻塞。
 * date: 18:15 2023/7/3
 *
 * @author beifengtz
 */
public class QuickFailManager<T> {

    private static final Logger logger = LoggerFactory.getLogger(QuickFailManager.class);

    static class FailedInfo {
        private int times;
        private final long startTime;

        public FailedInfo() {
            times = 1;
            startTime = System.currentTimeMillis();
        }
    }

    private final Map<T, FailedInfo> failedInfo = new ConcurrentHashMap<>();

    /**
     * 连续失败 n 次后触发快失败机制
     */
    private final int times;
    /**
     * 快失败机制 cd 时间后释放，单位 millis
     */
    private final int cd;

    /**
     * 创建一个新的快失败管理器
     *
     * @param times    连续失败 n 次后触发快失败机制
     * @param cdMillis 快失败机制 cd 时间后释放，单位 millis
     */
    public QuickFailManager(int times, int cdMillis) {
        this.times = times;
        this.cd = cdMillis;
    }

    /**
     * 检查是否可执行
     *
     * @param t 检查对象，可以是url也可以是某一个接口描述对象
     * @return true-可以执行，false-快失败中，无需执行直接返回失败
     */
    public boolean check(T t) {
        FailedInfo info = failedInfo.get(t);
        if (info == null) {
            return true;
        }
        if (info.times >= times) {
            if (System.currentTimeMillis() - info.startTime >= cd) {
                failedInfo.remove(t);
                logger.debug("Release quick fail by cd timeout: {}", t);
                return true;
            } else {
                logger.debug("Quick failed: {}", t);
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * 每一次真正执行之后都需要调用此方法，用于记录
     *
     * @param t       检查对象，可以是url也可以是某一个接口描述对象
     * @param success 此次实际执行是否成功
     */
    public void result(T t, boolean success) {
        if (success) {
            if (failedInfo.remove(t) != null) {
                logger.debug("Release quick fail by success: {}", t);
            }
        } else {
            failedInfo.compute(t, (key, info) -> {
                if (info == null) {
                    return new FailedInfo();
                }
                if (++info.times > times) {
                    logger.debug("New quick fail: {}", t);
                }
                return info;
            });
        }
    }

    /**
     * 强制打破快失败
     *
     * @param t 检查对象，可以是url也可以是某一个接口描述对象
     * @return 打破之前是否存在快失败
     */
    public boolean enforceBreak(T t) {
        return failedInfo.remove(t) != null;
    }
}
