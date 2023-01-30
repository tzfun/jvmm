package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.common.util.CommonUtil;
import org.beifengtz.jvmm.core.ext.os.OsProvider;
import org.junit.jupiter.api.Test;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

/**
 * Description: TODO
 * <p>
 * Created in 10:04 2022/9/30
 *
 * @author beifengtz
 */
public class TestOs {
    @Test
    public void testOsProvider() {
        OsProvider osProvider = OsProvider.get();
        System.out.println(osProvider.getNetIO());
        System.out.println(osProvider.getTcpState());
        System.out.println(osProvider.getDiskIO());
    }

    @Test
    public void testOshi() throws Exception {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hardware = si.getHardware();
        System.out.println(hardware);

        for (HWDiskStore disk : hardware.getDiskStores()) {
            CommonUtil.print(disk.getName(), disk.getModel(), disk.getReadBytes() / disk.getTransferTime(), disk.getWriteBytes() / disk.getTransferTime());
        }

        Thread.sleep(300);
        for (HWDiskStore disk : hardware.getDiskStores()) {
            CommonUtil.print(disk.getName(), disk.getModel(), disk.getReadBytes() / disk.getTransferTime(), disk.getWriteBytes() / disk.getTransferTime());
        }

        Thread.sleep(300);
        for (HWDiskStore disk : hardware.getDiskStores()) {
            CommonUtil.print(disk.getName(), disk.getModel(), disk.getReadBytes() / disk.getTransferTime(), disk.getWriteBytes() / disk.getTransferTime());
        }
    }
}
