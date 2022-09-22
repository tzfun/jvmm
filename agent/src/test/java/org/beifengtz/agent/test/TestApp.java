package org.beifengtz.agent.test;

import org.beifengtz.jvmm.agent.AgentBootStrap;
import org.beifengtz.jvmm.agent.util.AppUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.security.CodeSource;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 12:01 2021/5/22
 *
 * @author beifengtz
 */
public class TestApp {

    @Test
    public void testPath(){
        System.out.println(AppUtil.getLogPath());
        System.out.println(AppUtil.getDataPath());
    }

    @Test
    public void testAgentPath() throws Exception{
        CodeSource codeSource = AgentBootStrap.class.getProtectionDomain().getCodeSource();
        File agentJarFile = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
        System.out.println(agentJarFile);
    }
}
