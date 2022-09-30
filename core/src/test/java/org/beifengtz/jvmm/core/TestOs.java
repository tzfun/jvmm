package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.core.ext.os.OsProvider;
import org.junit.jupiter.api.Test;

/**
 * Description: TODO
 *
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
    }
}
