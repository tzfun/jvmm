package org.beifengtz.jvmm.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.jar.JarFile;

/**
 * <p>
 * Description: Jvmm agent boot
 * </p>
 * <p>
 * Created in 11:42 2021/5/22
 *
 * @author beifengtz
 */
public class AgentBootStrap {
    private static final Logger log = LoggerFactory.getLogger(AgentBootStrap.class);
    private static final String STATIC_LOGGER_BINDER_CLASS = "org/slf4j/impl/StaticLoggerBinder.class";
    private static final String STATIC_LOGGER_BINDER_PATH = "org.slf4j.impl.StaticLoggerBinder";
    private static final String JVMM_SERVER_JAR = "jvmm-server.jar";
    private static final String SERVER_MAIN_CLASS = "org.beifengtz.jvmm.server.ServerBootstrap";
    private static final String SERVER_CONFIG_CLASS = "org.beifengtz.jvmm.server.ServerConfig";

    private static volatile boolean running;
    private static volatile Thread bindThread;
    private static volatile JvmmAgentClassLoader agentClassLoader;
    /**
     * server模块是否已载入，如果已载入后续不需要重复装载
     */
    private static volatile boolean agentAttached;
    private static volatile Object bootInstance;

    private static final BlockingQueue<LoggerEvent> logQueue = new LinkedBlockingQueue<>();

    static {
//        tryFindSpringClassLoader();
    }

    private static ClassLoader loadWithSpringClassLoader() {
        try {
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

            //  为了兼容Spring项目，需寻找Spring自定义的ClassLoader。
            //  Spring项目在打成Jar包或者War包时会由特定的Launcher启动，启动时创建一个叫LaunchedURLClassLoader来加载jar包
            Class<?> springLauncher = null;
            try {
                springLauncher = Class.forName("org.springframework.boot.loader.JarLauncher", false, systemClassLoader);
                log.debug("The target program is a spring application and is started as a jar.");
            } catch (NoClassDefFoundError | ClassNotFoundException ignored1) {
                try {
                    springLauncher = Class.forName("org.springframework.boot.loader.WarLauncher", false, systemClassLoader);
                    log.debug("The target program is a spring application and is started as a war.");
                } catch (NoClassDefFoundError | ClassNotFoundException ignored2) {
                }
            }

            if (springLauncher != null) {
                //  spring的ClassLoader存在启动线程的上下文中，扫描线程对象下的ClassLoader来获取
                ClassLoader springLaunchClassLoader = null;
                Set<Thread> allThread = Thread.getAllStackTraces().keySet();
                for (Thread thread : allThread) {
                    ClassLoader contextClassLoader = thread.getContextClassLoader();
                    if (contextClassLoader != null && contextClassLoader.getClass().getName().startsWith("org.springframework.boot.loader.LaunchedURLClassLoader")) {
                        springLaunchClassLoader = contextClassLoader;
                        break;
                    }
                }

                if (springLaunchClassLoader != null) {
                    ClassLoader cl = AgentBootStrap.class.getClassLoader();
                    if (cl instanceof URLClassLoader) {
                        loadLogClassFromAnotherClassLoader((URLClassLoader) cl, springLaunchClassLoader);
                        FileUtil.delFile(new File(AppUtil.getTempPath()));
                    } else {
                        System.err.println("WARNING: Can not load log jar by classloader: " + cl.getClass().getName());
                        System.err.println("WARNING: " + cl.getClass().getName() + " is not java.net.URLClassLoader or its subclasses ");
                    }
                }
                return springLaunchClassLoader;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void loadLogClassFromAnotherClassLoader(URLClassLoader loader, ClassLoader another) throws Throwable {
        String logMsg = String.format("Class loader [%s] load logger class from [%s]", loader.getClass().getName(), another.getClass().getName());
        log.info(logMsg);

        loadResourceFromAnother(loader, another, STATIC_LOGGER_BINDER_CLASS);
        while (true) {
            try {
                Class.forName(STATIC_LOGGER_BINDER_PATH);
                logMsg = "Agent logger initialization is ok.";
                log.info(logMsg);
                break;
            } catch (NoClassDefFoundError e) {
                if (!loadResourceFromAnother(loader, another, e.getMessage() + ".class")) {
                    break;
                }
            } catch (ClassNotFoundException ignored) {
                break;
            }
        }
    }

    private static boolean loadResourceFromAnother(URLClassLoader loader, ClassLoader another, String classPath) throws Throwable {
        Enumeration<URL> loggerResources = another.getResources(classPath);
        while (loggerResources.hasMoreElements()) {
            String urlPath = loggerResources.nextElement().getFile();
            urlPath = urlPath.substring(5);
            String jarFilePath = urlPath.substring(0, urlPath.lastIndexOf("!"));
            String[] jars = jarFilePath.split("!/");
            if (jars.length > 0) {
                String baseJarPath = jars[0];
                String tmpPath = AppUtil.getTempPath();
                String tmpJar = null;
                //  递归的解析jar包
                for (int i = 1; i < jars.length; ++i) {
                    String relativePath = jars[i];
                    if (relativePath.endsWith(".jar")) {
                        if (FileUtil.findAndUnzipJar(tmpPath, baseJarPath, relativePath)) {
                            int idx = relativePath.lastIndexOf("/");
                            tmpPath += relativePath.substring(0, idx + 1);
                            tmpJar = relativePath.substring(idx + 1);
                        }
                    } else {
                        break;
                    }
                }
                if (tmpJar == null) {
                    return false;
                }
                String finalJarPath = tmpPath + tmpJar;

                URL jarFile = new File(finalJarPath).toURI().toURL();
                ClassLoaderUtil.classLoaderAddURL(loader, jarFile);
                log.info("Load jar file from " + finalJarPath);
                return true;
            }
        }
        return false;
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        main(agentArgs, inst, "premain");
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        main(agentArgs, inst, "agentmain");
    }

    /**
     * 代理载入 jvmm-server.jar 包，期间会由新的ClassLoader载入这些jar包，对一些需要预加载的jar包，在attach前会搜索过滤出这些jar
     * 然后再由自定义的ClassLoader载入。
     * <p>
     * 如果 jvmm-server.jar 未加载过，并且 server 未启动，将会执行：解析参数、下载jar依赖、共享日志jar包、加载jar包、启动服务、启动日志处理线程；
     * 如果 jvmm-server.jar 已加载过，并且 server 未启动，将会执行：解析参数、启动服务、启动日志处理线程；
     * 如果 jvmm-server.jar 已加载过，并且 server 已启动，操作将被禁止；
     *
     * @param args 参数
     * @param inst {@link Instrumentation}
     * @param type 载入类型：
     *             agentmain - 运行时动态载入
     *             premain - 启动时载入
     */
    private static synchronized void main(String args, final Instrumentation inst, String type) {
        log.info("Jvm monitor Agent attached by {}.", type);

        if (Objects.isNull(args)) {
            args = "";
        }

        int idx = args.indexOf(";");
        String serverJar;
        final String agentArgs;
        if (idx < 0) {
            serverJar = "";
            agentArgs = args.trim() + ";fromAgent=true";
        } else {
            serverJar = args.substring(0, idx).trim();
            agentArgs = args.substring(idx).trim() + ";fromAgent=true";
        }
        if ("agentmain".equals(type)) {
            if (agentAttached) {
                try {
                    log.info("The jvmm agent has been loaded once and enters the server startup phase...");
                    bootServer(inst, null, agentArgs);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
                return;
            }
            agentAttached = true;
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
            notifyListener(findListenerPortArg(agentArgs), "Jvmm server jar file not found");
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
                Enumeration<URL> loggerResources = loggerClassLoader.getResources(STATIC_LOGGER_BINDER_CLASS);
                while (loggerResources.hasMoreElements()) {
                    String urlPath = loggerResources.nextElement().getFile();
                    String jarFilePath1 = urlPath.substring(5).split("!")[0];
                    int chIdx = jarFilePath1.lastIndexOf("/");
                    String jarName = jarFilePath1.substring(chIdx);
                    if (jarName.contains("logback-classic")) {
                        String jarFilePath2 = jarFilePath1.substring(0, chIdx) + jarName.replace("classic", "core");
                        urlList.add(new File(jarFilePath2).toURI().toURL());
                    }
                    URL jarFile = new File(jarFilePath1).toURI().toURL();
                    urlList.add(jarFile);
                    needPreLoad.add(jarFile);
                }
                agentClassLoader = new JvmmAgentClassLoader(urlList.toArray(new URL[0]), ClassLoader.getSystemClassLoader());
            }

            bootServer(inst, needPreLoad, agentArgs);

        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            notifyListener(findListenerPortArg(agentArgs), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static int findListenerPortArg(String args) {
        String[] argKv = args.split(";");
        for (String s : argKv) {
            String[] split = s.split("=");
            if (split.length > 1 && "listenerPort".equalsIgnoreCase(split[0])) {
                return Integer.parseInt(split[1]);
            }
        }
        return -1;
    }

    /**
     * 通知client端启动进程
     */
    private static void notifyListener(int listenerPort, String message) {
        try {
            try (Socket socket = new Socket("127.0.0.1", listenerPort);
                 PrintWriter writer = new PrintWriter(socket.getOutputStream())) {
                writer.write(message);
            }
        } catch (Exception e) {
            log.error("Notify listener failed: " + e.getMessage(), e);
        }
    }

    private static void bootServer(Instrumentation inst, List<URL> needPreLoad, String agentArgs) throws InterruptedException {
        //  启动日志打印代理消费线程
        runLoggerConsumer();
        running = true;
        bindThread = new Thread(() -> {
            int listenerPort = findListenerPortArg(agentArgs);
            try {
                if (needPreLoad != null && !needPreLoad.isEmpty()) {
                    for (URL url : needPreLoad) {
                        ClassLoaderUtil.classLoaderAddURL(agentClassLoader, url);
                    }
                }

                Class<?> bootClazz = agentClassLoader.loadClass(SERVER_MAIN_CLASS);
                Object boot = bootClazz.getMethod("getInstance", Instrumentation.class, String.class)
                        .invoke(null, inst, agentArgs);
                bootInstance = boot;
                Function<Object, Object> callback = o -> {
                    notifyListener(listenerPort, o.toString());
                    return null;
                };
                bootClazz.getMethod("start", Function.class).invoke(boot, callback);
            } catch (Throwable e) {
                Throwable throwable = e;
                if (throwable instanceof InvocationTargetException) {
                    throwable = ((InvocationTargetException) throwable).getTargetException();
                    if (throwable == null) {
                        throwable = e;
                    }
                }
                running = false;
                log.error(throwable.getMessage(), throwable);
                notifyListener(listenerPort, throwable.getMessage());
            }
        });

        bindThread.setName("jvmm-binding");
        bindThread.setContextClassLoader(agentClassLoader);
        bindThread.start();
        bindThread.join(3000);
    }

    /**
     * 为了应用宿主程序的日志配置，启动一个日志消费者线程去Agent Server内部的处理日志
     */
    private static void runLoggerConsumer() {
        if (running) {
            return;
        }
        Thread thread = new Thread(() -> {
            while (running) {
                try {
                    LoggerEvent info = logQueue.take();
                    Logger logger = LoggerFactory.getLogger(info.getName());
                    switch (info.getType()) {
                        case "TRACE": {
                            if (info.getArgs() == null) {
                                if (info.getThrowable() == null) {
                                    logger.trace(info.getMsg());
                                } else {
                                    logger.trace(info.getMsg(), info.getThrowable());
                                }
                            } else {
                                logger.trace(info.getMsg(), info.getArgs());
                            }
                        }
                        break;
                        case "INFO": {
                            if (info.getArgs() == null) {
                                if (info.getThrowable() == null) {
                                    logger.info(info.getMsg());
                                } else {
                                    logger.info(info.getMsg(), info.getThrowable());
                                }
                            } else {
                                logger.info(info.getMsg(), info.getArgs());
                            }
                        }
                        break;
                        case "DEBUG": {
                            if (info.getArgs() == null) {
                                if (info.getThrowable() == null) {
                                    logger.debug(info.getMsg());
                                } else {
                                    logger.debug(info.getMsg(), info.getThrowable());
                                }
                            } else {
                                logger.debug(info.getMsg(), info.getArgs());
                            }
                        }
                        break;
                        case "WARN": {
                            if (info.getArgs() == null) {
                                if (info.getThrowable() == null) {
                                    logger.warn(info.getMsg());
                                } else {
                                    logger.warn(info.getMsg(), info.getThrowable());
                                }
                            } else {
                                logger.warn(info.getMsg(), info.getArgs());
                            }
                        }
                        break;
                        case "ERROR": {
                            if (info.getArgs() == null) {
                                if (info.getThrowable() == null) {
                                    logger.error(info.getMsg());
                                } else {
                                    logger.error(info.getMsg(), info.getThrowable());
                                }
                            } else {
                                logger.error(info.getMsg(), info.getArgs());
                            }
                        }
                        break;
                    }
                } catch (Throwable e) {
                    log.error("Consume log failed", e);
                }
            }
        });
        thread.setName("jvmm-logger");
        thread.start();
        log.info("Log agent thread started successfully.");
    }

    /**
     * 由Agent Server反射调用
     * <p>
     * 调用位置：org.beifengtz.jvmm.server.logger.DefaultLoggerAdaptor#publish(org.beifengtz.jvmm.common.logger.LoggerEvent)
     * <p>
     * 这里的参数不能直接用LoggerEvent，两个ClassLoader上下文不一样，会出现找不到方法的情况
     */
    public static boolean logger(Map<String, Object> event) {
        return logQueue.offer(LoggerEvent.fromMap(event));
    }

    /**
     * server模块中 org.beifengtz.jvmm.server.ServerBootstrap#stop() 调用
     */
    public static void serverStop() {
        running = false;
        if (bindThread != null) {
            bindThread.interrupt();
        }
    }
}
