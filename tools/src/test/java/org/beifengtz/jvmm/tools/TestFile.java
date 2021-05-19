package org.beifengtz.jvmm.tools;

import org.beifengtz.jvmm.tools.util.FileUtil;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 15:27 2021/5/22
 *
 * @author beifengtz
 */
public class TestFile {

    @Test
    public void testDownloadFile(){
        System.out.println(FileUtil.readFileFromNet("http://www.beifengtz.com","F:\\Project","test.html"));;
        System.out.println(FileUtil.readFileFromNet("111","F:\\Project","test.html"));;
    }

    @Test
    public void testUnzipJar() throws Exception{
//        FileUtil.unzipJar("E:\\MagicCube\\MagicCubeServer\\MCGameClientNew\\bin\\jvmm",
//                "E:\\MagicCube\\MagicCubeServer\\MCGameClientNew\\bin\\MCGameClientNew.jar");

        FileUtil.findAndUnzipJar("E:\\MagicCube\\MagicCubeServer\\MCGameClientNew\\bin\\jvmm",
                "E:\\MagicCube\\MagicCubeServer\\MCGameClientNew\\bin\\MCGameClientNew.jar", "BOOT-INF/lib/logback-classic-1.2.3.jar");
    }
}
