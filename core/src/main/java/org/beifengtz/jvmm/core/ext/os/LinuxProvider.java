package org.beifengtz.jvmm.core.ext.os;

import org.beifengtz.jvmm.common.util.ExecuteNativeUtil;
import org.beifengtz.jvmm.core.entity.result.OsNetIOResult;
import org.beifengtz.jvmm.core.entity.result.OsNetStateResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Description: TODO
 *
 * Created in 11:12 2022/9/29
 *
 * @author beifengtz
 */
class LinuxProvider extends OsScheduledService implements OsProvider {
    private static LinuxProvider INSTANCE;

    private final AtomicReference<OsNetIOResult> osNetIOResult = new AtomicReference<>(new OsNetIOResult());
    private final AtomicReference<OsNetStateResult> osTcpStateResult = new AtomicReference<>(new OsNetStateResult());

    private LinuxProvider() {
    }

    public synchronized static LinuxProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LinuxProvider();
            INSTANCE.start();
        }
        return INSTANCE;
    }

    @Override
    public OsNetIOResult getNetIO() {
        return osNetIOResult.get();
    }

    @Override
    public OsNetStateResult getTcpState() {
        return osTcpStateResult.get();
    }

    @Override
    public void run() {
        getTcpState0();
        getNetIO0();
    }

    private void getNetIO0() {
        long[] s1 = getNetStatistics();
        executor.schedule(() -> {
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
        List<String> result = ExecuteNativeUtil.execute("cat /proc/net/dev");
        long rb = 0, tb = 0, rc = 0, tc = 0;
        for (String line : result) {
            //  只查看以太网数据
            if (line.matches(".*eth0:.*")) {
                String[] split = result.get(4).trim().split("\\s+");
                rb = Long.parseLong(split[1]);
                rc = Long.parseLong(split[2]);

                tb = Long.parseLong(split[9]);
                tc = Long.parseLong(split[10]);
                break;
            }
        }
        return new long[]{rb, tb, rc, tc};
    }

    private void getTcpState0() {
        List<String> result = ExecuteNativeUtil.execute("ss -at");
        Map<String, Integer> statusMap = new HashMap<>();
        int count = 0;
        for (String line : result) {
            String[] split = line.trim().split("\\s+");
            if (split.length > 4 && !"State".equalsIgnoreCase(split[0])) {
                statusMap.put(split[0], statusMap.getOrDefault(split[0], 0) + 1);
                count++;
            }
        }
        osTcpStateResult.get().setStatusCount(statusMap).setTotal(count);
    }
}
