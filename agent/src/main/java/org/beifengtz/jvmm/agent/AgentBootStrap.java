package org.beifengtz.jvmm.agent;

import org.beifengtz.jvmm.agent.util.AppUtil;
import org.beifengtz.jvmm.agent.util.ClassLoaderUtil;
import org.beifengtz.jvmm.agent.util.FileUtil;
import org.beifengtz.jvmm.agent.util.LoggerUtil;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
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

    private static final String TEMP_PATH = ".jvmm";
    private static final String JVMM_SERVER_JAR = "jvmm-server.jar";
    private static final String SERVER_MAIN_CLASS = "org.beifengtz.jvmm.server.ServerBootstrap";
    private static volatile Instrumentation instrumentation;

    private static volatile boolean running;
    private static volatile boolean initializedAgentLogger;
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

    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
//        JvmmAOPInitializer.initTracing(inst);
        main(agentArgs, inst, "premain");
    }

    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
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
     * @param args 参数，由 ; 分割，例如： server=/home/beifengtz/jvmm/server.jar;config=E:\Project\jvmm-dev\jvmm-dev\config.yml;fromAgent=true
     * @param inst {@link Instrumentation}
     * @param type 载入类型：
     *             agentmain - 运行时动态载入
     *             premain - 启动时载入
     */
    private static synchronized void main(String args, final Instrumentation inst, String type) throws Exception {
        instrumentation = inst;
        LoggerUtil.info(AgentBootStrap.class, "Jvm monitor agent attached by " + type);
        LoggerUtil.debug(AgentBootStrap.class, "Jvmm agent args: " + args);

        if (Objects.isNull(args)) {
            args = "";
        }

        String serverJar = "";

        String[] split = args.split(";");
        for (String s : split) {
            String[] kv = s.split("=");
            if ("server".equalsIgnoreCase(kv[0])) {
                serverJar = kv[1];
                break;
            }
        }
        args += ";fromAgent=true";

        if ("agentmain".equals(type)) {
            if (agentAttached) {
                try {
                    LoggerUtil.info(AgentBootStrap.class, "The jvmm agent has been loaded once and enters the server startup phase...");
                    bootServer(inst, null, args);
                } catch (InterruptedException e) {
                    LoggerUtil.error(AgentBootStrap.class, e.getMessage(), e);
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
            if (serverJar.isEmpty()) {
                //  从agent中读取jar
                InputStream serverStream = AgentBootStrap.class.getResourceAsStream("/jvmm-server.jar");
                if (serverStream == null) {
                    LoggerUtil.warn(AgentBootStrap.class, "Can not found jvmm-server.jar file from agent");
                    serverJarFile = new File("");
                } else {
                    serverJarFile = new File(TEMP_PATH, JVMM_SERVER_JAR);
                    LoggerUtil.info(AgentBootStrap.class, "Copy jvmm server dependency from agent...");
                    serverJarFile.getParentFile().mkdirs();
                    if (serverJarFile.exists()) {
                        serverJarFile.delete();
                    }
                    Files.copy(serverStream, serverJarFile.toPath());
                }
            } else {
                serverJarFile = new File(serverJar);
            }
        }

        if (!serverJarFile.exists()) {
            LoggerUtil.warn(AgentBootStrap.class, "Can not found jvmm-server.jar file from args: " + args);

            //  如果从参数中未成功读取jar包，依次按以下路径去寻找jar包
            //  1. 目标程序根目录下的 jvmm-server.jar 包
            //  2. agent的资源目录下的 jvmm-server.jar 包
            LoggerUtil.info(AgentBootStrap.class, "Try to find jvmm-server.jar file from target program directory");
            serverJarFile = new File(AppUtil.getHomePath(), JVMM_SERVER_JAR);
            if (!serverJarFile.exists()) {
                LoggerUtil.warn(LoggerUtil.class, "Can not found jvmm-server.jar file from target program directory");

                LoggerUtil.info(LoggerUtil.class, "Try to find jvmm-server.jar file from agent jar directory");
                CodeSource codeSource = AgentBootStrap.class.getProtectionDomain().getCodeSource();
                if (codeSource != null) {
                    try {
                        File agentJarFile = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
                        serverJarFile = new File(agentJarFile.getParentFile(), JVMM_SERVER_JAR);
                        if (!serverJarFile.exists()) {
                            LoggerUtil.error(AgentBootStrap.class, "Can not found jvmm-server.jar file from agent jar directory");
                        }
                    } catch (Throwable e) {
                        LoggerUtil.error(AgentBootStrap.class, String.format("Can not found jvmm-server.jar file from %s. %s", codeSource.getLocation(), e.getMessage()), e);
                    }
                }
            }
        }

        if (!serverJarFile.exists()) {
            notifyListener(findListenerPortFromArgs(args), "ERROR: Jvmm server jar file not found");
            return;
        }

        try {
            //  拓展搜索范围
            inst.appendToSystemClassLoaderSearch(new JarFile(serverJarFile));

            //  需要预装载的文件
            List<URL> needPreLoad = new ArrayList<>();
            if (agentClassLoader == null) {
                List<URL> urlList = new LinkedList<>();
                urlList.add(serverJarFile.toURI().toURL());
                agentClassLoader = new JvmmAgentClassLoader(urlList.toArray(new URL[0]), ClassLoader.getSystemClassLoader());
            }

            bootServer(inst, needPreLoad, args);

        } catch (Throwable e) {
            LoggerUtil.error(AgentBootStrap.class, e.getMessage(), e);
            notifyListener(findListenerPortFromArgs(args), "ERROR: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static ClassLoader loadWithSpringClassLoader() {
        try {
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

            //  为了兼容Spring项目，需寻找Spring自定义的ClassLoader。
            //  Spring项目在打成Jar包或者War包时会由特定的Launcher启动，启动时创建一个叫LaunchedURLClassLoader来加载jar包
            Class<?> springLauncher = null;
            try {
                springLauncher = Class.forName("org.springframework.boot.loader.JarLauncher", false, systemClassLoader);
                LoggerUtil.debug(AgentBootStrap.class, "The target program is a spring application and is started as a jar");
            } catch (NoClassDefFoundError | ClassNotFoundException ignored1) {
                try {
                    springLauncher = Class.forName("org.springframework.boot.loader.WarLauncher", false, systemClassLoader);
                    LoggerUtil.debug(AgentBootStrap.class, "The target program is a spring application and is started as a war");
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
                return springLaunchClassLoader;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
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
                String tmpPath = TEMP_PATH;
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
                ClassLoaderUtil.loadJar(loader, jarFile);
                LoggerUtil.info(AgentBootStrap.class, "Load jar file from " + finalJarPath);
                return true;
            }
        }
        return false;
    }

    private static int findListenerPortFromArgs(String args) {
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
        if (listenerPort <= 0) {
            return;
        }
        try {
            try (Socket socket = new Socket("127.0.0.1", listenerPort);
                 PrintWriter writer = new PrintWriter(socket.getOutputStream())) {
                writer.write(message);
            }
        } catch (Exception e) {
            LoggerUtil.error(AgentBootStrap.class, "Notify listener failed: " + e.getMessage(), e);
        }
    }

    private static void bootServer(Instrumentation inst, List<URL> needPreLoad, String agentArgs) throws InterruptedException {
        running = true;
        //  启动日志打印代理消费线程
        runLoggerConsumer();
        bindThread = new Thread(() -> {
            int listenerPort = findListenerPortFromArgs(agentArgs);
            try {
                if (needPreLoad != null && !needPreLoad.isEmpty()) {
                    for (URL url : needPreLoad) {
                        ClassLoaderUtil.loadJar(agentClassLoader, url);
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
                LoggerUtil.error(AgentBootStrap.class, throwable.getMessage(), throwable);
                notifyListener(listenerPort, "ERROR: " + e.getClass().getName() + ":" + throwable.getMessage());
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
        if (initializedAgentLogger) {
            return;
        }
        Thread thread = new Thread(() -> {
            while (running) {
                try {
                    LoggerEvent info = logQueue.take();
                    String level = info.getLevel().toLowerCase();
                    if (info.getArgs() == null) {
                        if (info.getThrowable() == null) {
                            LoggerUtil.logger(info.getName(), level, info.getMsg());
                        } else {
                            LoggerUtil.logger(info.getName(), level, info.getMsg(), info.getThrowable());
                        }
                    } else {
                        LoggerUtil.logger(info.getName(), level, info.getMsg(), info.getArgs());
                    }
                } catch (Throwable e) {
                    LoggerUtil.error(AgentBootStrap.class, "Consume log failed", e);
                }
            }
        });
        thread.setName("jvmm-logger");
        thread.start();
        initializedAgentLogger = true;
        LoggerUtil.info(AgentBootStrap.class, "Agent logger thread started");
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

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }
}
