package org.beifengtz.jvmm.common.test;

import org.beifengtz.jvmm.common.util.FileUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.jar.JarFile;

/**
 * Description: TODO
 *
 * Created in 10:50 2022/9/27
 *
 * @author beifengtz
 */
public class TestFileUtil {
    @Test
    public void testCopyFromJar() throws Exception {
        JarFile jar = new JarFile("jvmm-server.jar");
        File target = new File(".jvmm/jar/server");
        String regex = "(async-profiler/.*|com/.*|io/.*|org/benf.*|org/slf4j.*|META-INF/maven/.*|META-INF/native/.*|META-INF/native-image/.*|io.netty.versions.propeties)";

        FileUtil.copyFromJar(jar, target, regex);
    }

    @Test
    public void testZip() throws Exception {
        File serverJarFile = new File("jvmm-server.jar");
        File target = new File(".jvmm/jar/server/");
        FileUtil.zip(target, serverJarFile, false);
    }
}
