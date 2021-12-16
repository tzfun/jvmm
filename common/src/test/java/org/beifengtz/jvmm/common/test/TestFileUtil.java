package org.beifengtz.jvmm.common.test;

import org.beifengtz.jvmm.common.util.FileUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Description: TODO
 *
 * Created in 18:19 2021/12/16
 *
 * @author beifengtz
 */
public class TestFileUtil {
    @Test
    public void testReadYml() throws IOException {
        System.out.println(FileUtil.readYml("E:\\Project\\jvmm-dev\\config.yml"));
    }
}
