package org.beifengtz.jvmm.client.test;

import org.beifengtz.jvmm.client.ClientBootstrap;
import org.beifengtz.jvmm.client.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 19:41 2021/5/14
 *
 * @author beifengtz
 */
public class TestConfig {

    @Test
    public void testParseArgs() throws Exception{
        Method parseConfig = ClientBootstrap.class.getDeclaredMethod("parseConfig", String.class);
        parseConfig.setAccessible(true);
        Configuration configuration = (Configuration)parseConfig.invoke(null, "config=F:\\Project\\jvmm\\client\\src\\main\\resources\\client.properties;port.bind=1000");
        Assertions.assertEquals(configuration.getPort(), 1000);
        Assertions.assertEquals(configuration.getName(), "jvmm_client");
    }
}
