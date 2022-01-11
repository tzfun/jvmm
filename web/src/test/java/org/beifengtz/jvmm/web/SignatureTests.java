package org.beifengtz.jvmm.web;

import org.beifengtz.jvmm.common.util.SignatureUtil;
import org.junit.Test;

/**
 * Description: TODO
 *
 * Created in 15:18 2022/1/11
 *
 * @author beifengtz
 */
public class SignatureTests {
    @Test
    public void testMd5() throws Exception {
        System.out.println(SignatureUtil.MD5("jvmm_pass"));
    }
}
