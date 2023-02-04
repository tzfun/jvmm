package org.beifengtz.jvmm.server.test;

import com.google.gson.Gson;
import org.beifengtz.jvmm.server.entity.conf.Configuration;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.nio.file.Files;
import java.nio.file.Paths;

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
        Configuration conf = yaml.loadAs(Files.newInputStream(Paths.get("config.yml")), Configuration.class);
        System.out.println(new Gson().toJson(conf));
    }
}
