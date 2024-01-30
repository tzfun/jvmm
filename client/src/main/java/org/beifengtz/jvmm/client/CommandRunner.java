package org.beifengtz.jvmm.client;

import io.netty.channel.EventLoopGroup;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.client.cli.CmdLine;
import org.beifengtz.jvmm.client.cli.CmdLineGroup;
import org.beifengtz.jvmm.client.cli.CmdOption;
import org.beifengtz.jvmm.client.cli.CmdParser;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.common.util.IOUtil;
import org.beifengtz.jvmm.common.util.PidUtil;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.common.util.meta.PairKey;
import org.beifengtz.jvmm.convey.channel.ChannelUtil;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.beifengtz.jvmm.core.driver.VMDriver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:21 上午 2021/12/11
 *
 * @author beifengtz
 */
public class CommandRunner {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(CommandRunner.class);
    private static final CmdLineGroup cmdGroup;

    static {
        cmdGroup = CmdLineGroup.create()
                .setHeadDesc("Below will list all of parameters. You need choose running mode firstly.")
                .addCommand(CmdLine.create()
                        .setKey("")
                        .setOrder(1)
                        .addOption(CmdOption.create()
                                .setName("m")
                                .setArgName("mode")
                                .setOrder(1)
                                .setDesc("* Choose running mode: client, attach, jar"))
                        .addOption(CmdOption.create()
                                .setName("h")
                                .setArgName("help")
                                .setOrder(2)
                                .setDesc("Help for usage."))
                        .setTailDesc("After you select the running mode, the next parameters will continue to be used, " +
                                "for example: executing `-m client` will enter client mode, or you can directly bring " +
                                "the parameters of the mode, eg. `-m client -a 127.0.0.1:5010`"));

        cmdGroup.addCommand(CmdLine.create()
                .setKey("client")
                .setHeadDesc("Connect to jvmm server and execute some commands.")
                .setOrder(2)
                .addOption(CmdOption.create()
                        .setName("a")
                        .setArgName("address")
                        .setOrder(1)
                        .setDesc("The address that will connect to the Jvmm server, like '127.0.0.1:5010'."))
                .addOption(CmdOption.create()
                        .setName("u")
                        .setArgName("username")
                        .setOrder(2)
                        .setDesc("Jvmm server authentication account. If the target jvmm server is auth enable."))
                .addOption(CmdOption.create()
                        .setName("p")
                        .setArgName("password")
                        .setOrder(3)
                        .setDesc("Jvmm server authentication password. If the target jvmm server is auth enable.")));

        cmdGroup.addCommand(CmdLine.create()
                .setKey("attach")
                .setHeadDesc("Attach jvmm server to another java program in this computer.")
                .setOrder(3)
                .addOption(CmdOption.create()
                        .setName("c")
                        .setArgName("config")
                        .setOrder(1)
                        .setDesc("The path of the config file, support relative path, absolute path and http(s) address. Required in attach mode."))
                .addOption(CmdOption.create()
                        .setName("p")
                        .setArgName("port")
                        .setOrder(2)
                        .setDesc("Target java program listening port. If pid is not filled in, this parameter is required."))
                .addOption(CmdOption.create()
                        .setName("pid")
                        .setArgName("pid")
                        .setOrder(3)
                        .setDesc("The pid of target java program. If port is not filled in, this parameter is required."))
                .addOption(CmdOption.create()
                        .setName("a")
                        .setArgName("agentJarFile")
                        .setOrder(4)
                        .setDesc("The path of the 'jvmm-agent.jar' file, support relative path, absolute path and network address. Required in attach mode."))
                .addOption(CmdOption.create()
                        .setName("s")
                        .setArgName("serverJarFile")
                        .setOrder(5)
                        .setDesc("The path of the 'jvmm-server.jar' file, support relative path, absolute path and network address. Required in attach mode."))
        );

        cmdGroup.addCommand(CmdLine.create()
                .setKey("jar")
                .setHeadDesc("Generate jvmm-agent.jar and jvmm-server.jar")
                .setOrder(4)
                .addOption(CmdOption.create()
                        .setName("a")
                        .setOrder(1)
                        .setDesc("Generate jvmm-agent.jar"))
                .addOption(CmdOption.create()
                        .setName("s")
                        .setOrder(2)
                        .setDesc("Generate jvmm-server.jar"))
                .addOption(CmdOption.create()
                        .setName("e")
                        .setOrder(3)
                        .setArgName("exclude")
                        .setDesc("Specifies the name of the dependencies to be excluded in the generated jar, optional value: slf4j"))
        );
    }

    public static void run(String[] args) throws Throwable {
        try {
            CmdParser cmd = CmdParser.parse(cmdGroup.getCommand(""), args);
            if (cmd.hasArg("h")) {
                printHelp();
                return;
            }

            String mode = null;
            if (cmd.hasArg("m")) {
                mode = cmd.getArg("m");
            } else {
                mode = GuidedRunner.askMode();
            }

            if ("attach".equalsIgnoreCase(mode)) {
                handleAttach(CmdParser.parse(cmdGroup.getCommand("attach"), args));
            } else if ("client".equalsIgnoreCase(mode)) {
                handleClient(CmdParser.parse(cmdGroup.getCommand("client"), args));
            } else if ("jar".equalsIgnoreCase(mode)) {
                handleGenerateJar(CmdParser.parse(cmdGroup.getCommand("jar"), args));
            } else {
                logger.error("Only allow model types: client, attach");
            }
        } catch (ParseException e) {
            printHelp();
        }
        System.exit(0);
    }

    private static void handleGenerateJar(CmdParser cmd) throws IOException {
        boolean generateAgent = cmd.hasArg("a");
        boolean generateServer = cmd.hasArg("s");

        if (!generateAgent && !generateServer) {
            char type = GuidedRunner.askGenerateJarType();
            if (type == 'a') {
                generateAgent = true;
            } else {
                generateServer = true;
            }
        }

        boolean containsSlf4j = true;
        //  生成 agent 才需要询问，生成 server 一般情况都需要包含
        boolean excluded = cmd.hasArg("e");
        if (excluded) {
            String exclude = cmd.getArg("e");
            if (exclude.contains("logger")) {
                containsSlf4j = false;
            } else if (generateAgent) {
                containsSlf4j = GuidedRunner.askImportSlf4j();
            }
        } else if (generateAgent) {
            containsSlf4j = GuidedRunner.askImportSlf4j();
        }

        if (generateAgent) {
            if (canGenerateAgentJar()) {
                generateAgentJar(null, containsSlf4j);
            } else {
                logger.error("Can not generate agent jar file, case: no runtime source.");
            }
        }

        if (generateServer) {
            if (canGenerateServerJar()) {
                generateServerJar(null, containsSlf4j);
            } else {
                logger.error("Can not generate server jar file, case: no runtime source");
            }
        }

        logger.info("Finished");
    }

    private static boolean canGenerateServerJar() {
        String path = CommandRunner.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return CommandRunner.class.getResourceAsStream("/server-source") != null && path.endsWith(".jar");
    }

    /**
     * 生成server的jar包
     *
     * @param outputDir     输出目录
     * @param containsSlf4j 是否包含SLF4J的API
     * @throws IOException IO异常
     */
    private static void generateServerJar(String outputDir, boolean containsSlf4j) throws IOException {
        if (canGenerateServerJar()) {
            File serverJarFile = new File(outputDir, "jvmm-server.jar");
            if (checkJarVersion(outputDir) && serverJarFile.exists() && checkJarSign(outputDir, "server", containsSlf4j)) {
                logger.info("The same version of jvmm-server.jar already exists in the output directory");
                return;
            }
            logger.info("Starting to generate server jar...");
            String path = CommandRunner.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            try {
                path = URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.warn("Decode path failed. " + e.getClass().getName() + ": " + e.getMessage());
            }

            File tempDir = new File(FileUtil.getTempPath(), "server");
            try {
                if (tempDir.exists()) {
                    FileUtil.delFile(tempDir);
                }
                List<String> libRegexList = getServerLibRegexList();

                if (containsSlf4j) {
                    libRegexList.add("org/slf4j/.*");
                    logger.info("The slf4j dependencies is put into server");
                } else {
                    logger.info("The slf4j dependencies is removed");
                }

                FileUtil.copyFromJar(new JarFile(path), tempDir, StringUtil.join("|", libRegexList), fileName -> {
                    if (fileName.startsWith("server-source")) {
                        return fileName.replace("server-source/", "");
                    } else {
                        return fileName;
                    }
                });

                FileUtil.zip(tempDir, serverJarFile, false);
                logger.info("Generated server jar to " + serverJarFile.getAbsolutePath());

                try {
                    File versionFile = getVersionFile(outputDir);
                    FileUtil.writeByteArrayToFile(versionFile, IOUtil.toByteArray(CommandRunner.class.getResourceAsStream("/version.txt")));
                    FileUtil.writeByteArrayToFile(getJarSignFile(outputDir, "server"), String.valueOf(containsSlf4j).getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    logger.warn("Write version file failed: " + e.getMessage());
                }
            } finally {
                if (tempDir.exists()) {
                    FileUtil.delFile(tempDir);
                }
            }
        } else {
            logger.error("The jvmm server cannot be generated. You can try the following: 1. select the appropriate jvmm version, 2. run in jar mode");
        }
    }

    private static List<String> getServerLibRegexList() {
        List<String> list = new ArrayList<>();
        list.add("async-profiler/.*");
        list.add("com/.*");
        list.add("io/.*");
        list.add("org/benf.*");
        list.add("META-INF/native/.*");
        list.add("META-INF/native-image/.*");
        list.add("server-source/.*");
        list.add(".*jvmm/common/.*");
        list.add(".*jvmm/convey/.*");
        list.add(".*jvmm/core/.*");
        list.add(".*jvmm/log/.*");
        list.add("oshi/.*");
        list.add("oshi.*");
        list.add("org/yaml.*");
        return list;
    }

    private static boolean checkJarVersion(String dir) throws IOException {
        //  已生成的文件存在并且版本相同则不再生成
        File versionFile = getVersionFile(dir);
        if (versionFile.exists()) {
            String existV = Files.readAllLines(versionFile.toPath()).get(0).trim();
            InputStream vis = CommandRunner.class.getResourceAsStream("/version.txt");
            if (vis != null) {
                String newV = IOUtil.toString(vis).trim();
                return Objects.equals(existV, newV);
            }
        }
        return false;
    }

    private static boolean checkJarSign(String dir, String name, boolean containsSlf4j) throws IOException {
        File signFile = getJarSignFile(dir, name);
        if (signFile.exists()) {
            return Objects.equals(String.valueOf(containsSlf4j), FileUtil.readFileToString(signFile, StandardCharsets.UTF_8).trim());
        }
        return false;
    }

    private static File getVersionFile(String dir) {
        if (StringUtil.isEmpty(dir)) {
            return new File(FileUtil.getTempPath(), ".version");
        }
        dir = dir.replaceAll("\\\\", "/");
        if (dir.endsWith("/")) {
            dir = dir.substring(0, dir.length() - 1);
        }

        return new File(dir.endsWith(FileUtil.getTempPath()) ? dir : (dir + "/" + FileUtil.getTempPath()), ".version");
    }

    private static File getJarSignFile(String dir, String name) {
        if (StringUtil.isEmpty(dir)) {
            return new File(FileUtil.getTempPath(), name + ".sign");
        }
        dir = dir.replaceAll("\\\\", "/");
        if (dir.endsWith("/")) {
            dir = dir.substring(0, dir.length() - 1);
        }

        return new File(dir.endsWith(FileUtil.getTempPath()) ? dir : (dir + "/" + FileUtil.getTempPath()), "server.sign");
    }

    private static boolean canGenerateAgentJar() {
        return CommandRunner.class.getResourceAsStream("/jvmm-agent.jar") != null;
    }

    private static void generateAgentJar(String outputDir, boolean containsSlf4j) throws IOException {
        if (canGenerateAgentJar()) {
            File agentJarFile = new File(outputDir, "jvmm-agent.jar");
            logger.info("Starting to generate agent jar...");
            if (checkJarVersion(outputDir) && agentJarFile.exists() && checkJarSign(outputDir, "agent", containsSlf4j)) {
                logger.info("The same version of jvmm-agent.jar already exists in the output directory");
                return;
            }
            File tempFile = FileUtil.createTempFile("jvmm-agent.jar.tmp");
            File tempDir = FileUtil.createTempDir("jvmm-agent.tmp");
            try {
                FileUtil.writeByteArrayToFile(tempFile, IOUtil.toByteArray(CommandRunner.class.getResourceAsStream("/jvmm-agent.jar")));
                //  解压 agent
                FileUtil.unJar(tempFile, tempDir);
                generateServerJar(tempDir.getAbsolutePath(), containsSlf4j);
                //  删除 server 生成的签名文件
                FileUtil.delFile(new File(tempDir, FileUtil.getTempPath()));
                logger.info("Copy server jar to agent...");
                FileUtil.zip(tempDir, agentJarFile, false);
            } finally {
                FileUtil.delFile(tempFile);
                FileUtil.delFile(tempDir);
            }

            logger.info("Generated agent jar to " + agentJarFile.getAbsolutePath());

            try {
                FileUtil.writeByteArrayToFile(getVersionFile(outputDir), IOUtil.toByteArray(CommandRunner.class.getResourceAsStream("/version.txt")));
                FileUtil.writeByteArrayToFile(getJarSignFile(outputDir, "agent"), String.valueOf(containsSlf4j).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                logger.warn("Write version file failed: " + e.getMessage());
            }
        } else {
            logger.error("The jvmm-agent.jar cannot be generated, please select the appropriate jvmm version.");
        }
    }

    private static void handleClient(CmdParser cmd) throws Throwable {
        String address = null, username = null, password = null;
        if (cmd.hasArg("a")) {
            address = cmd.getArg("a");
            if (cmd.hasArg("u")) {
                username = cmd.getArg("u");
            }
            if (cmd.hasArg("p")) {
                password = cmd.getArg("p");
            }
        } else {
            address = GuidedRunner.askServerAddress();
        }

        String[] split = address.split(":");
        if (split.length != 2) {
            logger.error("Invalid address: " + address);
            return;
        }
        String host = split[0];
        int port = Integer.parseInt(split[1]);

        EventLoopGroup group = ChannelUtil.newEventLoopGroup(1);

        JvmmConnector connector = tryConnect(host, port, group, username, password);

        connector.registerCloseListener(() -> {
            System.out.println();
            logger.info("Connected channel inactive, trigger to close connector...");
            System.exit(0);
        });

        Scanner sc = new Scanner(System.in);
        System.out.print("> ");
        while (connector.isConnected()) {
            String in = sc.nextLine();

            if ("exit".equalsIgnoreCase(in)) {
                logger.info("bye bye...");
                connector.close();
                group.shutdownGracefully();
                break;
            } else {
                try {
                    ServerService.handle(connector, in);
                } catch (Throwable e) {
                    logger.error("An exception occurred during processing.", e);
                }
            }

            if (!connector.isConnected()) {
                break;
            }
            System.out.print("> ");
        }
        sc.close();
    }

    private static JvmmConnector tryConnect(String host, int port, EventLoopGroup group, String username, String password) throws InterruptedException {
        JvmmConnector connector = JvmmConnector.newInstance(host, port, group, true, username, password);
        CompletableFuture<Boolean> authFuture = connector.connect();

        try {
            boolean success = authFuture.get(3, TimeUnit.SECONDS);
            if (success) {
                logger.info("Connect successful! You can use the 'help' command to learn how to use. Enter 'exit' to safely exit the connection.");
                return connector;
            } else {
                if (username != null && password != null) {
                    logger.error("Auth failed");
                }
                username = GuidedRunner.askServerAuthUsername();
                password = GuidedRunner.askServerAuthPassword();
                return tryConnect(host, port, group, username, password);
            }
        } catch (ExecutionException | TimeoutException e) {
            logger.error("Connect server failed! case: time out");
            System.exit(1);
        }
        return null;
    }

    private static void handleAttach(CmdParser cmd) throws Throwable {
        String agentFilePath = null, configFilePath = null;
        int pid = -1;

        if (cmd.hasArg("a")) {
            agentFilePath = cmd.getArg("a");
        } else if (canGenerateAgentJar()) {
            generateAgentJar(FileUtil.getTempPath(), GuidedRunner.askImportSlf4j());
            agentFilePath = new File(FileUtil.getTempPath(), "jvmm-agent.jar").getAbsolutePath();
        } else {
            agentFilePath = GuidedRunner.askAgentFilePath();
        }

//            FileUtil.delFromJar(serverFilePath, JVMM_LOGGER_REGEX);

        if (cmd.hasArg("c")) {
            configFilePath = cmd.getArg("c");
            File file = new File(configFilePath);
            if (file.exists()) {
                configFilePath = file.getAbsolutePath();
            } else {
                logger.error("Can not find config file from '{}'", configFilePath);
                configFilePath = GuidedRunner.askConfigFilePath();
            }
        } else {
            configFilePath = GuidedRunner.askConfigFilePath();
        }
        logger.info("Using config file: {}", configFilePath);

        if (cmd.hasArg("p")) {
            pid = (int) PidUtil.findProcessByPort(Integer.parseInt(cmd.getArg("p")));
        } else if (cmd.hasArg("pid")) {
            pid = Integer.parseInt(cmd.getArg("pid"));
        } else {
            pid = GuidedRunner.askAttachPid();
        }

        if (pid <= 0) {
            logger.error("Target java program not running.");
            return;
        }

        File agentFile;
        //  支持从网络下载jar包
        if (agentFilePath.startsWith("http://") || agentFilePath.startsWith("https://")) {
            logger.info("Start downloading jar file from {}", agentFilePath);
            boolean loaded = FileUtil.readFileFromNet(agentFilePath, FileUtil.getTempPath(), "jvmm-agent.jar");
            if (loaded) {
                agentFile = new File(FileUtil.getTempPath(), "jvmm-agent.jar");
            } else {
                logger.error("Can not download 'jvmm-agent.jar'");
                return;
            }
        } else {
            agentFile = new File(agentFilePath);
            if (!agentFile.exists()) {
                logger.error("Agent jar file not exists! {}", agentFile.getAbsolutePath());
                return;
            }
        }

        //  开启目标服务启动状态监听
        PairKey<Integer, Thread> pair = startAttachListener();
        if (pair.getLeft() <= 0) {
            logger.error("Start attach listener holder failed.");
            return;
        }
        String args = String.format("config=%s;listenerPort=%d", configFilePath, pair.getLeft());

        logger.info("Start to attach program {} ...", pid);
        try {
            VMDriver.get().attachAgent(pid, agentFile.getAbsolutePath(), args);
            pair.getRight().join(30000);
        } catch (Exception e) {
            logger.warn("An error was encountered while attaching: " + e.getMessage(), e);
        }
    }

    private static PairKey<Integer, Thread> startAttachListener() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger port = new AtomicInteger(-1);
        Thread thread = new Thread(() -> {
            try (ServerSocket server = new ServerSocket(0)) {
                port.set(server.getLocalPort());
                latch.countDown();
                while (true) {
                    Socket socket = server.accept();
                    try (Scanner sc = new Scanner(socket.getInputStream())) {
                        if (sc.hasNextLine()) {
                            String content = sc.nextLine();
                            if ("start".equals(content)) {
                                logger.info("Attach successful! Try to start or stop services...");
                            } else if (content.startsWith("info:")) {
                                logger.info(content.substring(content.indexOf(":") + 1));
                            } else if (content.startsWith("warn:")) {
                                logger.warn(content.substring(content.indexOf(":") + 1));
                            } else if (content.startsWith("ok:")) {
                                String[] split = content.split(":");
                                if ("new".equals(split[1])) {
                                    if ("sentinel".equals(split[2])) {
                                        logger.info("New service started: [sentinel]");
                                    } else {
                                        logger.info("New service started on {}: [{}]", split[3], split[2]);
                                    }
                                } else if ("ready".equals(split[1])) {
                                    if ("sentinel".equals(split[2])) {
                                        logger.info("Service already started: [sentinel]");
                                    } else {
                                        logger.info("Service already started on {}: [{}]", split[3], split[2]);
                                    }
                                } else {
                                    logger.info("==> {}", content);
                                }
                            } else if ("end".equals(content)) {
                                logger.info("Attach finished");
                                break;
                            } else {
                                logger.error(content);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                latch.countDown();
                logger.error("Attach listener error: " + e, e);
            }
        });
        thread.setDaemon(false);
        thread.start();
        latch.await(3, TimeUnit.SECONDS);
        return PairKey.of(port.get(), thread);
    }

    private static void printHelp() {
        cmdGroup.printHelp();
    }
}
