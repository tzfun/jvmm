package org.beifengtz.jvmm.tools;

import org.beifengtz.jvmm.tools.util.JavaEnvUtil;
import org.beifengtz.jvmm.tools.util.SystemPropertyUtil;
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
        System.out.println(SystemPropertyUtil.get("java.home"));
        System.out.println(JavaEnvUtil.findJavaProgram("jps"));
        System.out.println(JavaEnvUtil.findJavaProgram("jstat"));
    }
}
