package org.beifengtz.jvmm.client;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.common.util.IOUtil;
import org.beifengtz.jvmm.common.util.PidUtil;
import org.beifengtz.jvmm.common.util.meta.PairKey;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.beifengtz.jvmm.core.AttachProvider;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    private static final Logger logger = LoggerFactory.logger(CommandRunner.class);

    private static final String TEMP_DIR = ".jvmm";
    private static final Options options;
    private static final Options rootOptions;
    private static final Options attachOptions;
    private static final Options clientOptions;

    static {
        options = new Options();
        rootOptions = new Options();
        attachOptions = new Options();

        options.addOption("help", false, "Help information.");
        rootOptions.addOption("help", false, "Help information.");

        Option mode = Option.builder("m")
                .required(false)
                .hasArg()
                .argName("mode")
                .desc("* Choose action mode: 'client' or 'attach'")
                .build();
        options.addOption(mode);
        rootOptions.addOption(mode);

        Option a = Option.builder("a")
                .required(false)
                .hasArg()
                .argName("agentJarFile")
                .desc("The path of the 'jvmm-agent.jar' file, support relative path, absolute path and network address. Required in attach mode.")
                .build();
        options.addOption(a);
        attachOptions.addOption(a);

        Option s = Option.builder("s")
                .required(false)
                .hasArg()
                .argName("serverJarFile")
                .desc("The path of the 'jvmm-server.jar' file, support relative path, absolute path and network address. Required in attach mode.")
                .build();
        options.addOption(s);
        attachOptions.addOption(s);

        Option c = Option.builder("c")
                .required(false)
                .hasArg()
                .argName("config")
                .desc("The path of the config file, support relative path, absolute path and http(s) address. Required in attach mode.")
                .build();
        options.addOption(c);
        attachOptions.addOption(c);

        Option p = Option.builder("p")
                .required(false)
                .hasArg()
                .argName("port")
                .desc("Target java program listening port. If pid is not filled in, this parameter is required.")
                .build();
        options.addOption(p);
        attachOptions.addOption(p);

        Option pid = Option.builder("pid")
                .required(false)
                .hasArg()
                .argName("pid")
                .desc("The pid of target java program. If port is not filled in, this parameter is required.")
                .build();
        options.addOption(pid);
        attachOptions.addOption(pid);

        clientOptions = new Options();

        Option host = Option.builder("h")
                .required(false)
                .hasArg()
                .argName("address")
                .desc("The address that will connect to the Jvmm server, like '127.0.0.1:5010'.")
                .build();
        options.addOption(host);
        clientOptions.addOption(host);

        Option username = Option.builder("user")
                .required(false)
                .hasArg()
                .argName("username")
                .desc("Jvmm server authentication account. If the target jvmm server is auth enable.")
                .build();
        options.addOption(username);
        clientOptions.addOption(username);

        Option password = Option.builder("pass")
                .required(false)
                .hasArg()
                .argName("password")
                .desc("Jvmm server authentication password. If the target jvmm server is auth enable.")
                .build();
        options.addOption(password);
        clientOptions.addOption(password);
    }

    public static void run(String[] args) throws Throwable {
        //parser
        CommandLineParser parser = DefaultParser.builder().build();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("help")) {
                printHelp();
                return;
            }

            String mode = null;
            if (cmd.hasOption("m")) {
                mode = cmd.getOptionValue("m");
            } else {
                mode = GuidedRunner.askMode();
            }

            if ("attach".equalsIgnoreCase(mode)) {
                handleAttach(cmd);
            } else if ("client".equalsIgnoreCase(mode)) {
                handleClient(cmd);
            } else {
                logger.error("Only allow model types: client, attach");
            }
        } catch (ParseException e) {
            printHelp();
        }
        System.exit(0);
    }

    private static void handleClient(CommandLine cmd) throws Throwable {
        String address = null, username = null, password = null;
        if (cmd.hasOption("h")) {
            address = cmd.getOptionValue("h");
            username = cmd.getOptionValue("user");
            password = cmd.getOptionValue("pass");
        } else {
            address = GuidedRunner.askServerAddress();
            if (GuidedRunner.askServerAuthEnable()) {
                username = GuidedRunner.askServerAuthUsername();
                password = GuidedRunner.askServerAuthPassword();
            }
        }

        String[] split = address.split(":");
        if (split.length != 2) {
            logger.error("Invalid address: " + address);
            return;
        }
        String host = split[0];
        int port = Integer.parseInt(split[1]);

        EventLoopGroup group = ChannelInitializers.newEventLoopGroup(1);

        logger.info("Start to connect jvmm agent server...");
        JvmmConnector connector = JvmmConnector.newInstance(host, port, group, true, username, password);
        Future<Boolean> connectF = connector.connect();
        if (connectF.await(3, TimeUnit.SECONDS)) {
            if (connectF.getNow()) {
                logger.info("Connect successful! You can use the 'help' command to learn how to use. Enter 'exit' to safely exit the connection.");
            } else {
                return;
            }
        } else {
            logger.error("Connect server failed! case: time out");
            System.exit(-1);
            return;
        }

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

    private static void handleAttach(CommandLine cmd) throws Throwable {
        //  先删除临时文件
        FileUtil.delFile(new File(TEMP_DIR));

        InputStream agentIs = CommandRunner.class.getResourceAsStream("/jvmm-agent.jar");
        InputStream serverIs = CommandRunner.class.getResourceAsStream("/jvmm-server.jar");

        String agentFilePath = null, serverFilePath = null, configFilePath = null;
        int pid = -1;

        if (cmd.hasOption("a")) {
            agentFilePath = cmd.getOptionValue("a");
        } else if (agentIs == null) {
            agentFilePath = GuidedRunner.askAgentFilePath();
        } else {
            File tempFile = new File(TEMP_DIR, "jvmm-agent.jar");
            FileUtil.writeByteArrayToFile(tempFile, IOUtil.toByteArray(agentIs));
            agentFilePath = tempFile.getAbsolutePath();
        }

        if (cmd.hasOption("s")) {
            serverFilePath = cmd.getOptionValue("s");
        } else if (serverIs == null) {
            serverFilePath = GuidedRunner.askServerFilePath();
        } else {
            File tempFile = new File(TEMP_DIR, "jvmm-server.jar");
            FileUtil.writeByteArrayToFile(tempFile, IOUtil.toByteArray(serverIs));
            serverFilePath = tempFile.getAbsolutePath();
        }

        if (cmd.hasOption("c")) {
            configFilePath = cmd.getOptionValue("c");
            File file = new File(configFilePath);
            if (file.exists()) {
                configFilePath = file.getAbsolutePath();
            } else {
                logger.error("Can not find config file from '" + configFilePath + "'");
                configFilePath = GuidedRunner.askConfigFilePath();
            }
        } else {
            configFilePath = GuidedRunner.askConfigFilePath();
        }

        if (cmd.hasOption("p")) {
            pid = (int) PidUtil.findProcessByPort(Integer.parseInt(cmd.getOptionValue("p")));
        } else if (cmd.hasOption("pid")) {
            pid = Integer.parseInt(cmd.getOptionValue("pid"));
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
            logger.info("Start downloading jar file from " + agentFilePath);
            boolean loaded = FileUtil.readFileFromNet(agentFilePath, TEMP_DIR, "jvmm-agent.jar");
            if (loaded) {
                agentFile = new File(TEMP_DIR, "jvmm-agent.jar");
            } else {
                logger.error("Can not download 'jvmm-agent.jar'");
                return;
            }
        } else {
            agentFile = new File(agentFilePath);
            if (!agentFile.exists()) {
                logger.error("Agent jar file not exists! " + agentFile.getAbsolutePath());
                return;
            }
        }

        File serverFile;
        if (serverFilePath.startsWith("http://") || serverFilePath.startsWith("https://")) {
            logger.info("Start downloading jar file from " + serverFilePath);
            boolean loaded = FileUtil.readFileFromNet(serverFilePath, TEMP_DIR, "jvmm-server.jar");
            if (loaded) {
                serverFile = new File(TEMP_DIR, "jvmm-server.jar");
            } else {
                logger.error("Can not download 'jvmm-server.jar'");
                return;
            }
        } else {
            serverFile = new File(serverFilePath);
            if (!serverFile.exists()) {
                logger.error("Server jar file not exists! " + serverFile.getAbsolutePath());
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
            AttachProvider.getInstance().attachAgent(pid, agentFile.getAbsolutePath(), serverFile.getAbsolutePath(), args);
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
        final int width = 130;
        HelpFormatter helper = new HelpFormatter();

        helper.setSyntaxPrefix("Command usage");
        helper.printHelp(width, HelpFormatter.DEFAULT_OPT_PREFIX, "Below will list all of parameters. You need choose running mode firstly.\n\n", rootOptions, "\n");
        helper.setSyntaxPrefix("Attach mode");
        helper.printHelp(width, HelpFormatter.DEFAULT_OPT_PREFIX, "Attach jvmm server to another java program in this computer.\n\n", attachOptions, "\n");
        helper.setSyntaxPrefix("Client mode");
        helper.printHelp(width, HelpFormatter.DEFAULT_OPT_PREFIX, "Connect to jvmm server and execute some commands.\n\n", clientOptions, "\n");
    }
}
