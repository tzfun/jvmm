package org.beifengtz.jvmm.common.test;

import org.beifengtz.jvmm.common.util.CodingUtil;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarFile;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 6:53 下午 2022/1/13
 *
 * @author beifengtz
 */
public class TestUtil {
    @Test
    @SuppressWarnings("deprecation")
    public void testBytes2Hex() {
        byte[] target = "122nfsdfkjsndvo4rnfvdxnvsdfn你好halshou空尼基哇にっぽんご ㄴㅧㅫ；124@#¥%……&**（（".getBytes(StandardCharsets.UTF_8);
        String r1 = CodingUtil.bytes2HexStr(target);
        String r2 = CodingUtil.bytes2HexString(target);

        Assertions.assertEquals(r1, r2);
    }

    @Test
    public void testCopyFromJar() throws Exception {
        File file = new File("jvmm-server.jar");
        if (!file.exists()) {
            return;
        }
        JarFile jar = new JarFile(file);
        File target = new File(".jvmm/jar/server");
        String regex = "(async-profiler/.*|com/.*|io/.*|org/benf.*|org/slf4j.*|META-INF/maven/.*|META-INF/native/.*|META-INF/native-image/.*|io.netty.versions.propeties)";

        FileUtil.copyFromJar(jar, target, regex);
    }
}
