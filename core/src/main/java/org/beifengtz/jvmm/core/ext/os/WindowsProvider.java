package org.beifengtz.jvmm.core.ext.os;

import org.beifengtz.jvmm.common.util.ExecuteNativeUtil;
import org.beifengtz.jvmm.core.entity.result.OsDiskIO;
import org.beifengtz.jvmm.core.entity.result.OsNetIO;
import org.beifengtz.jvmm.core.entity.result.OsNetState;

import java.io.File;
import java.nio.file.FileSystem;
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

    private final AtomicReference<OsNetIO> osNetIOResult = new AtomicReference<>(new OsNetIO());
    private final AtomicReference<OsNetState> osTcpStateResult = new AtomicReference<>(new OsNetState());
    private final AtomicReference<OsDiskIO> osDiskIOResult = new AtomicReference<>(new OsDiskIO());

    private WindowsProvider() {
    }

    @Override
    public OsNetIO getNetIO() {
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
    public OsNetState getTcpState() {
        if (!isRunning()) {
            getTcpState0();
        }
        return osTcpStateResult.get();
    }

    @Override
    public OsDiskIO getDiskIO() {
        return osDiskIOResult.get();
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
            OsNetIO nsr = osNetIOResult.get();

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
