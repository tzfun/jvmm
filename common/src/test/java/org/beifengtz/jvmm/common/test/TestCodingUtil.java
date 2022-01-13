package org.beifengtz.jvmm.common.test;

import org.beifengtz.jvmm.common.util.CodingUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 6:53 下午 2022/1/13
 *
 * @author beifengtz
 */
public class TestCodingUtil {
    @Test
    @SuppressWarnings("deprecation")
    public void testBytes2Hex() {
        byte[] target = "122nfsdfkjsndvo4rnfvdxnvsdfn你好halshou空尼基哇にっぽんご ㄴㅧㅫ；124@#¥%……&**（（".getBytes(StandardCharsets.UTF_8);
        String r1 = CodingUtil.bytes2HexStr(target);
        String r2 = CodingUtil.bytes2HexString(target);

        Assertions.assertEquals(r1, r2);
    }
}
