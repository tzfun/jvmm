package org.beifengtz.jvmm.core.ext.os;

import org.beifengtz.jvmm.common.util.ExecuteNativeUtil;
import org.beifengtz.jvmm.core.entity.result.OsNetIOResult;
import org.beifengtz.jvmm.core.entity.result.OsNetStateResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Description: TODO
 * <p>
 * Created in 11:12 2022/9/29
 *
 * @author beifengtz
 */
class MacProvider extends OsScheduledService {
    static MacProvider INSTANCE = new MacProvider();

    private final AtomicReference<OsNetIOResult> osNetIOResult = new AtomicReference<>(new OsNetIOResult());
    private final AtomicReference<OsNetStateResult> osTcpStateResult = new AtomicReference<>(new OsNetStateResult());

    private MacProvider() {
    }

    @Override
    public OsNetIOResult getNetIO() {
        return osNetIOResult.get();
    }

    @Override
    public OsNetStateResult getTcpState() {
        if (!isRunning()) {
            getTcpState0();
        }
        return osTcpStateResult.get();
    }

    @Override
    public void run() {
        getTcpState0();
    }

    private void getTcpState0() {
        List<String> result = ExecuteNativeUtil.execute("netstat -anvp tcp");
        Map<String, Integer> statusMap = new HashMap<>();
        Set<Integer> pidSet = new HashSet<>();

        int count = 0;
        for (String line : result) {
            if (line.matches(".*tcp.*")) {
                String[] split = line.trim().split("\\s+");
                if (split.length > 8) {
                    int pid = Integer.parseInt(split[8]);
                    if (pidSet.add(pid)) {
                        String status = split[5];
                        statusMap.put(status, statusMap.getOrDefault(status, 0) + 1);
                        count++;
                    }
                }
            }
        }
        osTcpStateResult.get().setStatusCount(statusMap).setTotal(count);
    }
}
