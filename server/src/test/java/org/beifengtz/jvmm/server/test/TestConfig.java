package org.beifengtz.jvmm.server.test;

import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.server.entity.conf.Configuration;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.nio.file.Files;

/**
 * @author beifengtz
 * @description: TODO
 * @date 11:26 2023/2/2
 */
public class TestConfig {
    @Test
    public void testYmlParse() throws Exception {
        Yaml yaml = new Yaml();
        yaml.setBeanAccess(BeanAccess.FIELD);
        File configFile = new File("jvmm.yml");
        if (configFile.exists()) {
            Configuration conf = yaml.loadAs(Files.newInputStream(configFile.toPath()), Configuration.class);
            System.out.println(StringUtil.getGson().toJson(conf));
        }
    }
}
