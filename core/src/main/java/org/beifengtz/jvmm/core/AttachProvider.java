package org.beifengtz.jvmm.core;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.util.ClassLoaderUtil;
import org.beifengtz.jvmm.common.util.JavaEnvUtil;
import org.beifengtz.jvmm.common.util.JavaVersionUtils;
import org.beifengtz.jvmm.core.conf.Configuration;
import org.slf4j.Logger;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Properties;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 15:30 2021/5/17
 *
 * @author beifengtz
 */
public class AttachProvider {

    private static final Logger log = LoggerFactory.logger(AttachProvider.class);

    private static volatile AttachProvider INSTANCE;

    private volatile ClassLoader toolsClassLoader;

    private AttachProvider() throws Throwable {
        initToolsClassLoader();
    }

    public static synchronized AttachProvider getInstance() throws Throwable {
        if (INSTANCE == null) {
            INSTANCE = new AttachProvider();
        }
        return INSTANCE;
    }

    protected synchronized void initToolsClassLoader() throws Throwable {
        if (toolsClassLoader == null) {
            File toolsJar = JavaEnvUtil.findToolsJar(JavaEnvUtil.findJavaHome());
            try {
                toolsClassLoader = ClassLoaderUtil.systemLoadJar(toolsJar.toURI().toURL());
                log.debug("Init tools classes successful.");
            } catch (MalformedURLException e) {
                //  ignored
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                log.error("Init class loader failed. " + e.getMessage(), e);
                throw e;
            }
        }
    }

    public void attachAgent(long targetPid, String agentJarPath, String serverJarPath, Configuration config) throws Exception {
        if (targetPid <= 0) {
            throw new IllegalArgumentException("Can not attach to virtual machine with illegal pid " + targetPid);
        }
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
                    log.warn("Target VM JAVA_HOME is {}, jvmm-server JAVA_HOME is {}, try to set the same JAVA_HOME.",
                            targetSystemProperties.getProperty("java.home"), System.getProperty("java.home"));
                }
            }

            virtualMachine.loadAgent(agentJarPath.replaceAll("\\\\","/"),
                    serverJarPath.replaceAll("\\\\","/") + ";" + config.argFormat());
        } finally {
            if (null != virtualMachine) {
                virtualMachine.detach();
            }
        }
    }
}
