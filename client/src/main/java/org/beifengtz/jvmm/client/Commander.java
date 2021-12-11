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
    private static final HelpFormatter helper;

    static {
        options = new Options();
        helper = new HelpFormatter();

        options.addOption("h", false, "Help information.");
        options.addOption("p", true, "Target java program listening port.");
        options.addOption("pid", true, "The pid of target java program.");

        Option a = Option.builder("a")
                .required(true)
                .hasArg()
                .argName("agent file path")
                .desc("The path of the 'jvmm-agent.jar' file. Support relative path, absolute path and network address.")
                .build();
        options.addOption(a);

        Option s = Option.builder("s")
                .required(true)
                .hasArg()
                .argName("server file path")
                .desc("The path of the 'jvmm-server.jar' file. Support relative path, absolute path and network address.")
                .build();
        options.addOption(s);

        Option c = Option.builder("c")
                .required(false)
                .hasArg()
                .argName("configuration")
                .desc("Agent startup configuration parameters, if not filled in, the default configuration will be used.")
                .build();
        options.addOption(c);

        Option p = Option.builder("p")
                .required(false)
                .hasArg()
                .argName("port")
                .desc("Target java program listening port. If pid is not filled in, this parameter is required.")
                .build();
        options.addOption(p);

        Option pid = Option.builder("pid")
                .required(false)
                .hasArg()
                .argName("pid")
                .desc("The pid of target java program. If port is not filled in, this parameter is required.")
                .build();
        options.addOption(pid);
    }

    public static void parse(String[] args) throws Throwable {
        //parser
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                printHelp();
                return;
            }
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
            run(pid, agentFile.getAbsolutePath(), serverFile.getAbsolutePath(), conf);

        } catch (ParseException e) {
            printHelp();
        }
    }

    private static void printHelp() {
        System.out.println();
        helper.printHelp(130, HelpFormatter.DEFAULT_OPT_PREFIX, "Attach jvmm server to another java program in this computer.\n", options, "\n");
    }

    private static void run(long pid, String agentPath, String serverPath, Configuration conf) throws Throwable {
        AttachProvider.getInstance().attachAgent(pid, agentPath, serverPath, conf);
    }
}
