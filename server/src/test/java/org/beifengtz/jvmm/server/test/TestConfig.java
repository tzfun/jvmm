package org.beifengtz.jvmm.server.test;

import org.beifengtz.jvmm.server.Configuration;
import org.beifengtz.jvmm.server.ServerBootstrap;
import org.beifengtz.jvmm.tools.util.SystemPropertyUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:35 2021/5/18
 *
 * @author beifengtz
 */
public class TestConfig {
    @Test
    public void testParseArgs() throws Exception{
        String homePath = SystemPropertyUtil.get("user.dir").replaceAll("\\\\", "/");

        Method parseConfig = ServerBootstrap.class.getDeclaredMethod("parseConfig", String.class);
        parseConfig.setAccessible(true);
        Configuration configuration = (Configuration)parseConfig.invoke(null, "config="+homePath+"/src/main/resources/jvmm-server.yml;port.bind=1000");
        Assertions.assertEquals(configuration.getPort(), 1000);
        Assertions.assertEquals(configuration.getName(), "jvmm_server");
    }
}
