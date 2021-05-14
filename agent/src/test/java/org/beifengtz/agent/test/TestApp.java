package org.beifengtz.agent.test;

import org.beifengtz.jvmm.agent.AppUtil;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 12:01 2021/5/22
 *
 * @author beifengtz
 */
public class TestApp {

    @Test
    public void testPath(){
        System.out.println(AppUtil.getLogPath());
        System.out.println(AppUtil.getDataPath());

    }
}
