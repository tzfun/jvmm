package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.tools.util.PidUtil;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 9:48 下午 2021/6/22
 *
 * @author beifengtz
 */
public class TestFlame {

    @Test
    public void testFlameFile() throws Exception{
        JvmmFactory.getExecutor().flameProfile((int) PidUtil.currentPid(), 20);
    }
}
