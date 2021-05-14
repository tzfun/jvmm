package org.beifengtz.jvmm.agent;

import org.beifengtz.jvmm.tools.JvmmClassLoader;
import org.beifengtz.jvmm.tools.util.CodingUtil;
import org.beifengtz.jvmm.tools.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.util.Objects;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:42 2021/5/22
 *
 * @author beifengtz
 */
public class AgentBootStrap {
    private static final Logger log = LoggerFactory.getLogger(AgentBootStrap.class);

    private static final String JVMM_CLIENT_JAR = "jvmm-client.jar";
    private static final String CLIENT_MAIN_CLASS = "org.beifengtz.jvmm.client.ClientBootStrap";

    private static volatile ClassLoader agentClassLoader;

    public static void premain(String agentArgs, Instrumentation inst) {
        main(agentArgs, inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        main(agentArgs, inst);
    }

    private static synchronized void main(String args, final Instrumentation inst) {
        try {
            Class<?> configClazz = Class.forName("org.beifengtz.jvmm.client.ClientConfig");
            Method isInited = configClazz.getMethod("isInited");
            if ((boolean) isInited.invoke(null)) {
                log.info("Jvmm client already started.");
                return;
            }
        } catch (Throwable ignored) {
        }

        if (Objects.isNull(args)) {
            args = "";
        }

        args = CodingUtil.decodeUrl(args);
        int idx = args.indexOf(";");
        String clientJar;
        final String agentArgs;
        if (idx < 0) {
            clientJar = "";
            agentArgs = args.trim();
        } else {
            clientJar = args.substring(0, idx).trim();
            agentArgs = args.substring(idx).trim();
        }

        File clientJarFile = null;
        //  支持从网络下载jar包
        if (clientJar.startsWith("http://") || clientJar.startsWith("https://")) {
            boolean loaded = FileUtil.readFileFromNet(clientJar, AppUtil.getDataPath(), JVMM_CLIENT_JAR);
            if (loaded) {
                clientJarFile = new File(AppUtil.getDataPath(), JVMM_CLIENT_JAR);
            } else {
                clientJarFile = new File("");
            }
        } else {
            //  从本地读取jar
            clientJarFile = new File(clientJar);
        }

        if (!clientJarFile.exists()) {
            log.warn("Can not found jvmm-client.jar file from args: {}", clientJar);

            //  如果从参数中未成功读取jar包，依次按以下路径去寻找jar包
            //  1. 目标程序根目录下的 jvmm-client.jar 包
            //  2. agent的资源目录下的 jvmm-client.jar 包

            log.info("Try to find jvmm-client.jar file from target program directory.");
            clientJarFile = new File(AppUtil.getHomePath(), JVMM_CLIENT_JAR);
            if (!clientJarFile.exists()) {
                log.warn("Can not found jvmm-client.jar file from target program directory.");

                log.info("Try to find jvmm-client.jar file from agent jar directory.");
                CodeSource codeSource = AgentBootStrap.class.getProtectionDomain().getCodeSource();
                if (codeSource != null) {
                    try {
                        File agentJarFile = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
                        clientJarFile = new File(agentJarFile.getParentFile(), JVMM_CLIENT_JAR);
                        if (!clientJarFile.exists()) {
                            log.error("Can not found jvmm-client.jar file from agent jar directory.");
                        }
                    } catch (Throwable e) {
                        log.error(String.format("Can not found jvmm-client.jar file from %s. %s", codeSource.getLocation(), e.getMessage()), e);
                    }
                }
            }
        }

        if (!clientJarFile.exists()) {
            return;
        }

        try {
            if (agentClassLoader == null) {
                agentClassLoader = new JvmmClassLoader(new URL[]{clientJarFile.toURI().toURL()});
            }

            bind(inst, agentClassLoader, agentArgs);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static void bind(Instrumentation inst, ClassLoader classLoader, String args) throws Throwable {
        Class<?> bootClazz = classLoader.loadClass(CLIENT_MAIN_CLASS);
        Object boot = bootClazz.getMethod("getInstance", Instrumentation.class, String.class).invoke(null, inst, args);
        int port = (int) bootClazz.getMethod("getPort").invoke(boot);
        if (port < 0) {
            throw new RuntimeException("Jvmm client start filed!");
        } else {
            log.info("Jvmm client already started on {}.", port);
        }
    }
}
