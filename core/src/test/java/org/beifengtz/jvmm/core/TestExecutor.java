package org.beifengtz.jvmm.core;

import org.junit.jupiter.api.Test;

/**
 * Created in 17:32 2021/5/12
 *
 * @author beifengtz
 */
public class TestExecutor {

    @Test
    public void testListProcess() {
        JvmmExecutor executor = JvmmFactory.getExecutor();
        System.out.println(executor.listJavaProcess());
    }
}
