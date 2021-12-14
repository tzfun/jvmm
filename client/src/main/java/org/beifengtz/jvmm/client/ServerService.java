package org.beifengtz.jvmm.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.beifengtz.jvmm.client.annotation.JvmmCmdDesc;
import org.beifengtz.jvmm.client.annotation.JvmmOption;
import org.beifengtz.jvmm.client.annotation.JvmmOptions;
import org.beifengtz.jvmm.common.exception.ErrorStatusException;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 8:11 下午 2021/12/11
 *
 * @author beifengtz
 */
public class ServerService {

    protected static final Map<String, Method> methodMap = new HashMap<>();
    protected static final Map<String, Options> optionsMap = new HashMap<>();
    protected static final Map<String, String> descMap = new HashMap<>();
    protected static final CommandLineParser commandParser = DefaultParser.builder().build();
    protected static final String HELP_KEY = "help";

    static {
        init();
    }

    protected static void init() {
        Method[] methods = ServerServiceImpl.class.getMethods();
        for (Method method : methods) {
            String name = method.getName();
            JvmmOption[] arr;
            if (method.isAnnotationPresent(JvmmOptions.class)) {
                JvmmOptions ops = method.getAnnotation(JvmmOptions.class);
                arr = ops.value();
            } else if (method.isAnnotationPresent(JvmmOption.class)) {
                arr = new JvmmOption[]{method.getAnnotation(JvmmOption.class)};
            } else if (method.isAnnotationPresent(JvmmCmdDesc.class)) {
                arr = new JvmmOption[0];
                descMap.put(name, method.getAnnotation(JvmmCmdDesc.class).desc());
            } else {
                continue;
            }

            methodMap.put(name, method);
            if (method.isAnnotationPresent(JvmmCmdDesc.class)) {
                descMap.put(name, method.getAnnotation(JvmmCmdDesc.class).desc());
            }

            Options options = new Options();
            for (JvmmOption op : arr) {
                Option.Builder builder = Option.builder(op.name()).hasArg(op.hasArg()).required(op.required());
                if (op.hasArg() && StringUtil.nonEmpty(op.argName())) {
                    builder.argName(op.argName());
                }
                if (StringUtil.nonEmpty(op.desc())) {
                    builder.desc(op.desc());
                }
                options.addOption(builder.build());
            }

            optionsMap.put(name, options);
        }
    }

    public static void handle(JvmmConnector connector, String command) throws Throwable {
        command = command.trim();
        if (command.isEmpty()) {
            return;
        }
        String[] split = command.split("\\s");
        if (split.length == 0) {
            return;
        }
        String key = split[0];

        String[] args;
        if (split.length > 1) {
            args = new String[split.length - 1];
            System.arraycopy(split, 1, args, 0, args.length);
        } else {
            args = new String[0];
        }

        if (key.equalsIgnoreCase(HELP_KEY)) {
            printHelp(args);
            return;
        }

        if (!methodMap.containsKey(key)) {
            printErr("Unknown command: " + key);
            return;
        }

        try {
            CommandLine cmd = commandParser.parse(optionsMap.get(key), args);
            methodMap.get(key).invoke(null, connector, cmd);
            Thread.sleep(50);
        } catch (ParseException e) {
            printErr("Invalid command arguments, case: " + e.getMessage());
        }
    }

    protected static void printHelp(String... names) {
        final int width = 130;
        HelpFormatter helper = new HelpFormatter();

        //  打印全部
        if (names == null || names.length == 0) {
            names = methodMap.keySet().toArray(new String[0]);
        }

        System.out.println();
        for (String name : names) {
            helper.setSyntaxPrefix(name + ": ");
            helper.printHelp(width, HelpFormatter.DEFAULT_OPT_PREFIX, descMap.get(name), optionsMap.get(name), "\n");
        }
    }

    protected static JvmmResponse request(JvmmConnector connector, JvmmRequest request) {
        return request(connector, request, 5, TimeUnit.SECONDS);
    }

    protected static JvmmResponse request(JvmmConnector connector, JvmmRequest request, long timeout, TimeUnit timeUnit) {
        try {
            return connector.waitForResponse(request, timeout, timeUnit);
        } catch (ErrorStatusException e) {
            printErr(String.format("Wrong response status: '%s', msg: %s", e.getStatus(), e.getMessage()));
        } catch (InterruptedException | TimeoutException e) {
            printErr("Request failed: " + e.getMessage());
        }
        return null;
    }

    protected static void printErr(String str) {
        if (Charset.defaultCharset() == StandardCharsets.UTF_8) {
            System.out.format("\33[31;1m%s\33[30;0m%n", str);
        } else {
            System.out.format("%s%n", str);
        }
    }

}
