package org.beifengtz.jvmm.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.common.util.PidUtil;
import org.beifengtz.jvmm.core.AttachProvider;
import org.beifengtz.jvmm.core.conf.ConfigParser;
import org.beifengtz.jvmm.core.conf.Configuration;
import org.slf4j.Logger;

import java.io.File;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:21 上午 2021/12/11
 *
 * @author beifengtz
 */
public class Commander {

    private static final Logger logger = LoggerFactory.logger(Commander.class);

    private static final String TEMP_DIR = "lib";
    private static final Options options;
    private static final Options attachOptions;
    private static final Options clientOptions;

    static {
        options = new Options();
        attachOptions = new Options();


        options.addOption("help", false, "Help information.");

        Option mode = Option.builder("m")
                .required(false)
                .hasArg()
                .argName("mode")
                .desc("Choose action mode: 'client' or 'attach', default value is client")
                .build();
        options.addOption(mode);

        Option a = Option.builder("a")
                .required(true)
                .hasArg()
                .argName("agentJarFile")
                .desc("The path of the 'jvmm-agent.jar' file. Support relative path, absolute path and network address.")
                .build();
        options.addOption(a);
        attachOptions.addOption(a);

        Option s = Option.builder("s")
                .required(true)
                .hasArg()
                .argName("serverJarFile")
                .desc("The path of the 'jvmm-server.jar' file. Support relative path, absolute path and network address.")
                .build();
        options.addOption(s);
        attachOptions.addOption(s);

        Option c = Option.builder("c")
                .required(false)
                .hasArg()
                .argName("config")
                .desc("Agent startup configuration parameters, if not filled in, the default configuration will be used.")
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
                .desc("The address that will connect to the Jvmm server, like '127.0.0.1：5010'.")
                .build();
        options.addOption(host);
        clientOptions.addOption(host);

        Option username = Option.builder("u")
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

    public static void parse(String[] args) throws Throwable {
        //parser
        CommandLineParser parser = DefaultParser.builder().build();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("help")) {
                printHelp();
                return;
            }
            String mode = cmd.getOptionValue("m");
            if ("attach".equals(mode)) {
                handleAttach(cmd);
            } else {
                handleClient(cmd);
            }
        } catch (ParseException e) {
            printHelp();
        }
    }

    private static void handleClient(CommandLine cmd) {
        logger.info("Not support now.");
    }

    private static void handleAttach(CommandLine cmd) throws Throwable {
        if (!cmd.hasOption("a")) {
            logger.error("Missing required parameter: jvmm-agent.jar path");
            printHelp();
            return;
        }

        if (!cmd.hasOption("s")) {
            logger.error("Missing required parameter: jvmm-server.jar path");
            printHelp();
            return;
        }

        if (!cmd.hasOption("p") && !cmd.hasOption("pid")) {
            logger.error("Missing required parameter: port or pid");
            printHelp();
            return;
        }

        String agentPath = cmd.getOptionValue("a");
        File agentFile;
        //  支持从网络下载jar包
        if (agentPath.startsWith("http://") || agentPath.startsWith("https://")) {
            boolean loaded = FileUtil.readFileFromNet(agentPath, TEMP_DIR, "jvmm-agent.jar");
            if (loaded) {
                agentFile = new File(TEMP_DIR, "jvmm-agent.jar");
            } else {
                logger.error("Can not load 'jvmm-agent.jar' from " + agentPath);
                return;
            }
        } else {
            agentFile = new File(agentPath);
            if (!agentFile.exists()) {
                logger.error("File not exists! " + agentFile.getAbsolutePath());
                return;
            }
        }

        String serverPath = cmd.getOptionValue("s");
        File serverFile;
        if (serverPath.startsWith("http://") || serverPath.startsWith("https://")) {
            boolean loaded = FileUtil.readFileFromNet(agentPath, TEMP_DIR, "jvmm-server.jar");
            if (loaded) {
                serverFile = new File(TEMP_DIR, "jvmm-server.jar");
            } else {
                logger.error("Can not load 'jvmm-server.jar' from " + agentPath);
                return;
            }
        } else {
            serverFile = new File(serverPath);
            if (!serverFile.exists()) {
                logger.error("File not exists! " + serverFile.getAbsolutePath());
                return;
            }
        }

        long pid = -1;
        if (cmd.hasOption("pid")) {
            pid = Integer.parseInt(cmd.getOptionValue("pid"));
        } else if (cmd.hasOption("p")) {
            pid = PidUtil.findProcessByPort(Integer.parseInt(cmd.getOptionValue("p")));
        }

        if (pid < 0) {
            logger.error("Target java program not running.");
        }

        Configuration conf;
        if (cmd.hasOption("c")) {
            conf = ConfigParser.parseFromArgs(cmd.getOptionValue("c"));
        } else {
            conf = Configuration.defaultInstance();
        }
        logger.info("Start to attach program {} ...", pid);
        AttachProvider.getInstance().attachAgent(pid, agentFile.getAbsolutePath(), serverFile.getAbsolutePath(), conf);
        logger.info("Attach successful!");
    }

    private static void printHelp() {
        final int width = 130;
        HelpFormatter helper = new HelpFormatter();

        helper.setSyntaxPrefix("Command usage");
        helper.printHelp(width, HelpFormatter.DEFAULT_OPT_PREFIX, "Below will list all of parameters. You need choose running mode firstly.\n\n", options, "\n");

        System.out.println("The following explains the interpretation of the parameters that may be used in each mode:\n");
        helper.setSyntaxPrefix("Attach mode");
        helper.printHelp(width, HelpFormatter.DEFAULT_OPT_PREFIX, "Attach jvmm server to another java program in this computer.\n\n", attachOptions, "\n");
        helper.setSyntaxPrefix("Client mode");
        helper.printHelp(width, HelpFormatter.DEFAULT_OPT_PREFIX, "Connect to jvmm server and execute some commands.\n\n", clientOptions, "\n");
    }
}
