package org.beifengtz.jvmm.client;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.beifengtz.jvmm.tools.util.CodingUtil;
import org.beifengtz.jvmm.tools.util.JavaEnvUtil;
import org.beifengtz.jvmm.tools.util.JavaVersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * <p>
 * Description: TODO 本类负责搜索物理机内可用的client，及向目标VM attach agent
 * </p>
 * <p>
 * Created in 10:10 上午 2021/5/16
 *
 * @author beifengtz
 */
public class ClientWatchDog {
    private static final Logger log = LoggerFactory.getLogger(ClientWatchDog.class);

    private static volatile ClientWatchDog INSTANCE;

    private volatile ClassLoader toolsClassLoader;

    private ClientWatchDog() {
    }

    public static synchronized ClientWatchDog getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClientWatchDog();
        }
        return INSTANCE;
    }

    private synchronized ClassLoader getToolsClassLoader() {
        if (toolsClassLoader == null) {
            File toolsJar = JavaEnvUtil.findToolsJar(JavaEnvUtil.findJavaHome());
            try {
                URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

                Method addURL = systemClassLoader.getClass().getSuperclass().getDeclaredMethod("addURL", URL.class);
                addURL.setAccessible(true);
                addURL.invoke(systemClassLoader, toolsJar.toURI().toURL());
                toolsClassLoader = systemClassLoader;
            } catch (MalformedURLException e) {
                //  ignored
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                log.error("Init class loader failed. " + e.getMessage(), e);
            }
        }
        return toolsClassLoader;
    }

    public void initToolsClasses() throws Throwable {
        ClassLoader classLoader = getToolsClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        classLoader.loadClass("com.sun.tools.attach.VirtualMachine");
        classLoader.loadClass("com.sun.tools.attach.VirtualMachineDescriptor");

        log.info("Init tools classes successful.");
    }

    private void attachAgent(long targetPid, String agentPath, String clientPath, ClientConfiguration config) throws Exception {
        VirtualMachineDescriptor virtualMachineDescriptor = null;
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            String pid = descriptor.id();
            if (pid.equals(Long.toString(targetPid))) {
                virtualMachineDescriptor = descriptor;
                break;
            }
        }
        VirtualMachine virtualMachine = null;
        try {
            if (null == virtualMachineDescriptor) {
                virtualMachine = VirtualMachine.attach(Long.toString(targetPid));
            } else {
                virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
            }

            Properties targetSystemProperties = virtualMachine.getSystemProperties();
            String targetJavaVersion = JavaVersionUtils.javaVersionStr(targetSystemProperties);
            String currentJavaVersion = JavaVersionUtils.javaVersionStr();
            if (targetJavaVersion != null && currentJavaVersion != null) {
                if (!targetJavaVersion.equals(currentJavaVersion)) {
                    log.warn("Current VM java version: {} do not match target VM java version: {}, attach may fail.",
                            currentJavaVersion, targetJavaVersion);
                    log.warn("Target VM JAVA_HOME is {}, jvmm-client JAVA_HOME is {}, try to set the same JAVA_HOME.",
                            targetSystemProperties.getProperty("java.home"), System.getProperty("java.home"));
                }
            }

            virtualMachine.loadAgent(CodingUtil.encodeUrl(agentPath), CodingUtil.encodeUrl(clientPath) + ";" + config.argFormat());
        } finally {
            if (null != virtualMachine) {
                virtualMachine.detach();
            }
        }
    }
}
