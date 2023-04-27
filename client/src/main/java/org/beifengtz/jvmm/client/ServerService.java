package org.beifengtz.jvmm.client;

import org.beifengtz.jvmm.client.annotation.IgnoreCmdParse;
import org.beifengtz.jvmm.client.annotation.JvmmCmdDesc;
import org.beifengtz.jvmm.client.annotation.JvmmOption;
import org.beifengtz.jvmm.client.annotation.JvmmOptions;
import org.beifengtz.jvmm.client.annotation.Order;
import org.beifengtz.jvmm.client.cli.CmdLine;
import org.beifengtz.jvmm.client.cli.CmdLineGroup;
import org.beifengtz.jvmm.client.cli.CmdOption;
import org.beifengtz.jvmm.client.cli.CmdParser;
import org.beifengtz.jvmm.common.exception.ErrorStatusException;
import org.beifengtz.jvmm.common.util.CommonUtil;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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
    protected static final Set<String> ignoreParseMap = new HashSet<>();
    protected static final String HELP_KEY = "help";
    protected static CmdLineGroup cmdGroup;

    static {
        init();
    }

    protected static void init() {
        cmdGroup = CmdLineGroup.create();

        Method[] methods = ServerServiceImpl.class.getMethods();
        for (Method method : methods) {
            String name = method.getName();

            CmdLine cmdLine = CmdLine.create().setKey(name);

            JvmmOption[] arr = null;
            if (method.isAnnotationPresent(JvmmOptions.class)) {
                JvmmOptions ops = method.getAnnotation(JvmmOptions.class);
                arr = ops.value();
            } else if (method.isAnnotationPresent(JvmmOption.class)) {
                arr = new JvmmOption[]{method.getAnnotation(JvmmOption.class)};
            }

            if (method.isAnnotationPresent(JvmmCmdDesc.class)) {
                JvmmCmdDesc cmdDesc = method.getAnnotation(JvmmCmdDesc.class);
                cmdLine.setHeadDesc(cmdDesc.headDesc())
                        .setTailDesc(cmdDesc.tailDesc());
            } else if (arr == null) {
                continue;
            }

            if (method.isAnnotationPresent(Order.class)) {
                cmdLine.setOrder(method.getAnnotation(Order.class).value());
            }

            methodMap.put(name, method);
            if (method.isAnnotationPresent(IgnoreCmdParse.class)) {
                ignoreParseMap.add(name);
            }
            if (arr != null) {
                for (JvmmOption op : arr) {
                    cmdLine.addOption(CmdOption.create()
                            .setName(op.name())
                            .setArgName(op.argName())
                            .setOrder(op.order())
                            .setDesc(op.desc()));
                }
            }
            cmdGroup.addCommand(cmdLine);
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
            Object arg;
            if (ignoreParseMap.contains(key)) {
                arg = CommonUtil.join(" ", Arrays.stream(args).collect(Collectors.toList()));
            } else {
                arg = CmdParser.parse(cmdGroup.getCommand(key), command);
            }
            methodMap.get(key).invoke(null, connector, arg);
            Thread.sleep(50);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (ParseException e) {
            printErr("Invalid command arguments, case: " + e.getMessage());
        }
    }

    protected static void printHelp(String... names) {
        //  打印全部
        if (names == null || names.length == 0) {
            cmdGroup.printHelp();
        } else {
            for (String name : names) {
                cmdGroup.printHelp(name);
            }
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
