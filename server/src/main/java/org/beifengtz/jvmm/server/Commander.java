package org.beifengtz.jvmm.server;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.beifengtz.jvmm.tools.util.PidUtil;

import java.io.File;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 8:15 下午 2021/12/10
 * xx文件的绝对路径
 *
 * @author beifengtz
 */
public class Commander {

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
                .desc("The path of the 'jvmm-agent.jar' file.")
                .build();
        options.addOption(a);

        Option s = Option.builder("s")
                .required(true)
                .hasArg()
                .argName("server file path")
                .desc("The path of the 'jvmm-server.jar' file.")
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
                printErr("Missing required parameter: jvmm-agent.jar path");
                printHelp();
                return;
            }

            if (!cmd.hasOption("s")) {
                printErr("Missing required parameter: jvmm-server.jar path");
                printHelp();
                return;
            }

            if (!cmd.hasOption("p") && !cmd.hasOption("pid")) {
                printErr("Missing required parameter: port or pid");
                printHelp();
                return;
            }

            File agentFile = new File(cmd.getOptionValue("a"));
            if (!agentFile.exists()) {
                printErr("File not exists! " + agentFile.getAbsolutePath());
                return;
            }

            File serverFile = new File(cmd.getOptionValue("s"));
            if (!serverFile.exists()) {
                printErr("File not exists! " + serverFile.getAbsolutePath());
                return;
            }

            long pid = -1;
            if (cmd.hasOption("pid")) {
                pid = Integer.parseInt(cmd.getOptionValue("pid"));
            } else if (cmd.hasOption("p")) {
                pid = PidUtil.findProcessByPort(Integer.parseInt(cmd.getOptionValue("p")));
            }

            if (pid < 0) {
                printErr("Target java program not running.");
            }

            Configuration conf;
            if (cmd.hasOption("c")) {
                conf = ServerBootstrap.parseConfig(cmd.getOptionValue("c"));
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
        helper.printHelp(HelpFormatter.DEFAULT_OPT_PREFIX, options);
        System.out.println();
    }

    private static void printErr(String msg) {
        System.err.println("[Error] " + msg);
    }

    private static void run(long pid, String agentPath, String serverPath, Configuration conf) throws Throwable {
        AttachProvider.getInstance().attachAgent(pid, agentPath, serverPath, conf);
    }
}
