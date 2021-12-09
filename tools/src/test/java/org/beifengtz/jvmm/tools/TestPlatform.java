package org.beifengtz.jvmm.tools;

import org.beifengtz.jvmm.tools.util.CommonUtil;
import org.beifengtz.jvmm.tools.util.PidUtil;
import org.beifengtz.jvmm.tools.util.PlatformUtil;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 9:57 2021/5/13
 *
 * @author beifengtz
 */
public class TestPlatform {

    @Test
    public void testProcessFind() {
        long pid = PidUtil.findProcessByPort(8700);
        System.out.println(pid);
    }

    @Test
    public void testPortAvailable() {
        long start = System.currentTimeMillis();
        CommonUtil.print(8700, "Port available:", PlatformUtil.portAvailable(8700));
        CommonUtil.print("Use time", System.currentTimeMillis() - start);
        CommonUtil.print(8000, "Port available:", PlatformUtil.portAvailable(8000));
        CommonUtil.print("Use time", System.currentTimeMillis() - start);
        CommonUtil.print(8010, "Port available:", PlatformUtil.portAvailable(8010));
        CommonUtil.print("Use time", System.currentTimeMillis() - start);
        CommonUtil.print(8081, "Port available:", PlatformUtil.portAvailable(8081));

        CommonUtil.print("Use time", System.currentTimeMillis() - start);
    }
}
