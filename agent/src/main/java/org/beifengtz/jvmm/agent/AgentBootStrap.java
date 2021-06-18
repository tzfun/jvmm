package org.beifengtz.jvmm.agent;

import org.beifengtz.jvmm.tools.JvmmClassLoader;
import org.beifengtz.jvmm.tools.util.ClassLoaderUtil;
import org.beifengtz.jvmm.tools.util.CodingUtil;
import org.beifengtz.jvmm.tools.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;

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

    private static final String JVMM_SERVER_JAR = "jvmm-server.jar";
    private static final String SERVER_MAIN_CLASS = "org.beifengtz.jvmm.server.ServerBootstrap";

    private static volatile ClassLoader agentClassLoader;
    private static volatile boolean premainAttached;

    public static void premain(String agentArgs, Instrumentation inst) {
        main(agentArgs, inst, "premain");
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        main(agentArgs, inst, "agentmain");
    }

    private static synchronized void main(String args, final Instrumentation inst, String type) {
        if ("premain".equals(type)) {
            if (premainAttached) {
                return;
            }
            premainAttached = true;
        }
        log.info("Agent attached by {}.", type);
        try {
            Class<?> configClazz = Class.forName("org.beifengtz.jvmm.server.ServerConfig");
            Method isInited = configClazz.getMethod("isInited");
            if ((boolean) isInited.invoke(null)) {
                log.info("Jvmm server already inited.");
                Method getRealBindPort = configClazz.getMethod("getRealBindPort");
                int realBindPort = (int) getRealBindPort.invoke(null);
                if (realBindPort >= 0) {
                    log.info("Jvmm server already started on {}", realBindPort);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }

        if (Objects.isNull(args)) {
            args = "";
        }

        args = CodingUtil.decodeUrl(args);
        int idx = args.indexOf(";");
        String serverJar;
        final String agentArgs;
        if (idx < 0) {
            serverJar = "";
            agentArgs = args.trim();
        } else {
            serverJar = args.substring(0, idx).trim();
            agentArgs = args.substring(idx).trim();
        }

        File serverJarFile = null;
        //  支持从网络下载jar包
        if (serverJar.startsWith("http://") || serverJar.startsWith("https://")) {
            boolean loaded = FileUtil.readFileFromNet(serverJar, AppUtil.getDataPath(), JVMM_SERVER_JAR);
            if (loaded) {
                serverJarFile = new File(AppUtil.getDataPath(), JVMM_SERVER_JAR);
            } else {
                serverJarFile = new File("");
            }
        } else {
            //  从本地读取jar
            serverJarFile = new File(serverJar);
        }

        if (!serverJarFile.exists()) {
            log.warn("Can not found jvmm-server.jar file from args: {}", serverJar);

            //  如果从参数中未成功读取jar包，依次按以下路径去寻找jar包
            //  1. 目标程序根目录下的 jvmm-server.jar 包
            //  2. agent的资源目录下的 jvmm-server.jar 包

            log.info("Try to find jvmm-server.jar file from target program directory.");
            serverJarFile = new File(AppUtil.getHomePath(), JVMM_SERVER_JAR);
            if (!serverJarFile.exists()) {
                log.warn("Can not found jvmm-server.jar file from target program directory.");

                log.info("Try to find jvmm-server.jar file from agent jar directory.");
                CodeSource codeSource = AgentBootStrap.class.getProtectionDomain().getCodeSource();
                if (codeSource != null) {
                    try {
                        File agentJarFile = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
                        serverJarFile = new File(agentJarFile.getParentFile(), JVMM_SERVER_JAR);
                        if (!serverJarFile.exists()) {
                            log.error("Can not found jvmm-server.jar file from agent jar directory.");
                        }
                    } catch (Throwable e) {
                        log.error(String.format("Can not found jvmm-server.jar file from %s. %s", codeSource.getLocation(), e.getMessage()), e);
                    }
                }
            }
        }

        if (!serverJarFile.exists()) {
            return;
        }

        try {
            //  拓展搜索范围
            inst.appendToSystemClassLoaderSearch(new JarFile(serverJarFile));

            //  需要预装载的文件，这里共享装载logger
            List<URL> needPreLoad = new ArrayList<>();
            if (agentClassLoader == null) {
                List<URL> urlList = new LinkedList<>();
                urlList.add(serverJarFile.toURI().toURL());
                ClassLoader loggerClassLoader = LoggerFactory.class.getClassLoader();
                Enumeration<URL> loggerResources = loggerClassLoader.getResources("org/slf4j/impl/StaticLoggerBinder.class");
                while (loggerResources.hasMoreElements()) {
                    URL jarFile = new File(loggerResources.nextElement().getFile().substring(6).split("!")[0]).toURI().toURL();
                    urlList.add(jarFile);
                    needPreLoad.add(jarFile);
                }
                agentClassLoader = new JvmmClassLoader(urlList.toArray(new URL[0]), loggerClassLoader);
            }

            Thread bindThread = new Thread(() -> {
                try {
                    for (URL url : needPreLoad) {
                        ClassLoaderUtil.systemLoadJar(url);
                    }
                    bind(inst, agentClassLoader, agentArgs);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            });

            bindThread.setName("jvmm-bind-thread");
            bindThread.setContextClassLoader(ClassLoader.getSystemClassLoader());
            bindThread.start();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static void bind(Instrumentation inst, ClassLoader classLoader, String args) throws Throwable {
        Class<?> bootClazz = classLoader.loadClass(SERVER_MAIN_CLASS);
        Object boot = bootClazz.getMethod("getInstance", Instrumentation.class, String.class).invoke(null, inst, args);
        bootClazz.getMethod("start").invoke(boot);
    }
}
