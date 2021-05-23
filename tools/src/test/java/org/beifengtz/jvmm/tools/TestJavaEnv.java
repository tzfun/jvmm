package org.beifengtz.jvmm.tools;

import org.beifengtz.jvmm.tools.util.CommonUtil;
import org.beifengtz.jvmm.tools.util.JavaEnvUtil;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:36 2021/5/12
 *
 * @author beifengtz
 */
public class TestJavaEnv {

    @Test
    public void testJavaHome(){
        long start = System.currentTimeMillis();
        System.out.println(JavaEnvUtil.findJavaProgram("jps"));
        CommonUtil.print("Use time", System.currentTimeMillis() - start);
        System.out.println(JavaEnvUtil.findJavaProgram("jstat"));
        CommonUtil.print("Use time", System.currentTimeMillis() - start);
    }
}
