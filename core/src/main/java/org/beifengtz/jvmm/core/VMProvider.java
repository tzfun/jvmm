package org.beifengtz.jvmm.core;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.util.ClassLoaderUtil;
import org.beifengtz.jvmm.common.util.IOUtil;
import org.beifengtz.jvmm.common.util.JavaEnvUtil;
import org.beifengtz.jvmm.common.util.JavaVersionUtils;
import org.beifengtz.jvmm.common.util.PidUtil;
import org.slf4j.Logger;
import sun.tools.attach.HotSpotVirtualMachine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
public class VMProvider {

    private static final Logger log = LoggerFactory.logger(VMProvider.class);

    private static volatile VMProvider INSTANCE;

    private volatile ClassLoader toolsClassLoader;

    private VMProvider() throws Throwable {
        initToolsClassLoader();
    }

    public static synchronized VMProvider getInstance() throws Throwable {
        if (INSTANCE == null) {
            INSTANCE = new VMProvider();
        }
        return INSTANCE;
    }

    protected synchronized void initToolsClassLoader() throws Throwable {
        if (toolsClassLoader == null) {
            String javaHome = JavaEnvUtil.findJavaHome();
            File toolsJar = JavaEnvUtil.findToolsJar(javaHome);

            if (JavaVersionUtils.isLessThanJava9()) {
                if (toolsJar == null || !toolsJar.exists()) {
                    throw new IllegalArgumentException("Can not find tools.jar under java home: " + javaHome);
                }
                try {
                    toolsClassLoader = ClassLoaderUtil.systemLoadJar(toolsJar.toURI().toURL());
                    log.debug("Init tools classes successful.");
                } catch (MalformedURLException e) {
                    //  ignored
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    log.error("Init class loader failed. " + e.getMessage(), e);
                    throw e;
                }
            } else {
                toolsClassLoader = ClassLoader.getSystemClassLoader();
            }
        }
    }

    public void attachAgent(long targetPid, String agentJarPath, String serverJarPath, String args) throws Exception {
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
            virtualMachine.loadAgent(agentJarPath.replaceAll("\\\\", "/"),
                    serverJarPath.replaceAll("\\\\", "/") + ";" + args);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Non-numeric value found")) {
                log.warn("An exception occurred when attaching: {}", e.getMessage());
            }
        } finally {
            if (virtualMachine != null) {
                virtualMachine.detach();
            }
        }
    }

    public String heapHisto() throws Exception {
        HotSpotVirtualMachine vm = (HotSpotVirtualMachine) VirtualMachine.attach(Long.toString(PidUtil.currentPid()));
        try {
            InputStream is = vm.heapHisto("-live");
            return IOUtil.toString(is);
        } finally {
            vm.detach();
        }
    }
}
