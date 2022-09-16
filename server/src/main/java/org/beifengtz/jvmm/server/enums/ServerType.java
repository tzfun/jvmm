package org.beifengtz.jvmm.server.enums;

import java.util.Locale;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:59 2022/9/7
 *
 * @author beifengtz
 */
public enum ServerType {
    none,
    /**
     * 默认运行类型，tcp长连接，并对通讯数据加密
     */
    jvmm,
    /**
     * http server服务
     */
    http,
    /**
     * 哨兵模式，定向采集数据并推送给订阅者
     */
    sentinel;

    public static ServerType of(String t) {
        return valueOf(t.toLowerCase(Locale.ROOT));
    }
}
