package org.beifengtz.jvmm.core.ext.os;

import org.beifengtz.jvmm.common.util.ExecuteNativeUtil;
import org.beifengtz.jvmm.core.entity.result.OsNetIOResult;
import org.beifengtz.jvmm.core.entity.result.OsNetStateResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Description: TODO
 *
 * Created in 11:12 2022/9/29
 *
 * @author beifengtz
 */
class WindowsProvider extends OsScheduledService {
    static WindowsProvider INSTANCE = new WindowsProvider();

    private final AtomicReference<OsNetIOResult> osNetIOResult = new AtomicReference<>(new OsNetIOResult());
    private final AtomicReference<OsNetStateResult> osTcpStateResult = new AtomicReference<>(new OsNetStateResult());

    private WindowsProvider() {
    }

    @Override
    public OsNetIOResult getNetIO() {
        if (!isRunning()) {
            try {
                getNetIO0().get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Os provider getNetIO error. " + e.getClass().getName() + ":" + e.getMessage(), e);
            }
        }
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
        getNetIO0();
    }

    private ScheduledFuture<?> getNetIO0() {
        long[] s1 = getNetStatistics();
        return executor.schedule(() -> {
            long[] s2 = getNetStatistics();
            OsNetIOResult nsr = osNetIOResult.get();

            long receivedB = s2[0] - s1[0];
            long transmittedB = s2[1] - s1[1];

            int receivedC = (int) (s2[2] - s1[2]);
            int transmittedC = (int) (s2[3] - s1[3]);
            nsr.setReceivePackageCount(receivedC)
                    .setTransmitPackageCount(transmittedC)
                    .setReceiveSpeed(receivedC == 0 ? 0 : ((float) receivedB / receivedC))
                    .setTransmitSpeed(transmittedC == 0 ? 0 : ((float) transmittedB / transmittedC));

        }, 1, TimeUnit.SECONDS);
    }

    private long[] getNetStatistics() {
        List<String> result = ExecuteNativeUtil.execute("netstat -e");
        long rb = 0, tb = 0, rc = 0, tc = 0;
        if (result.size() > 4) {
            String[] split = result.get(4).trim().split("\\s+");
            rb = Long.parseLong(split[1]);
            tb = Long.parseLong(split[2]);

            split = result.get(5).trim().split("\\s+");
            rc = Long.parseLong(split[1]);
            tc = Long.parseLong(split[2]);
        }
        return new long[]{rb, tb, rc, tc};
    }

    private void getTcpState0() {
        List<String> result = ExecuteNativeUtil.execute("netstat -an");
        Map<String, Integer> statusMap = new HashMap<>();
        int count = 0;
        for (String line : result) {
            if (line.matches(".*TCP.*")) {
                String[] split = line.trim().split("\\s+");
                if (split.length > 3) {
                    String status = split[3];
                    statusMap.put(status, statusMap.getOrDefault(status, 0) + 1);
                    count++;
                }
            }
        }
        osTcpStateResult.get().setStatusCount(statusMap).setTotal(count);
    }
}
