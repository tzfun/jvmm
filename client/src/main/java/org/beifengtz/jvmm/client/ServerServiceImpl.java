package org.beifengtz.jvmm.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.beifengtz.jvmm.client.annotation.IgnoreCmdParse;
import org.beifengtz.jvmm.client.annotation.JvmmCmdDesc;
import org.beifengtz.jvmm.client.annotation.JvmmOption;
import org.beifengtz.jvmm.client.annotation.JvmmOptions;
import org.beifengtz.jvmm.client.annotation.Order;
import org.beifengtz.jvmm.client.cli.CmdParser;
import org.beifengtz.jvmm.common.util.CodingUtil;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.beifengtz.jvmm.core.CollectionType;
import org.beifengtz.jvmm.core.entity.result.JpsResult;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Description: TODO
 * Created in 4:07 下午 2021/12/12
 *
 * @author beifengtz
 */
public class ServerServiceImpl extends ServerService {

    @JvmmOptions({
            @JvmmOption(
                    name = "t",
                    argName = "type",
                    order = 1,
                    desc = "Collection type, optional values: \n- process\n- disk\n- disk_io\n- cpu" +
                            "\n- network\n- sys\n- sys_memory\n- sys_file\n- jvm_classloading\n- jvm_classloader" +
                            "\n- jvm_compilation\n- jvm_gc\n- jvm_memory\n- jvm_memory_manager\n- jvm_memory_pool" +
                            "\n- jvm_thread\n- jvm_thread_stack\n- jvm_thread_detail\n- jvm_thread_pool"
            ),
            @JvmmOption(
                    name = "f",
                    argName = "file",
                    order = 2,
                    desc = "Output file. If the output file is specified, the output content is saved to the file, otherwise output on the screen"
            ),
            @JvmmOption(
                    name = "tid",
                    argName = "threadId",
                    order = 3,
                    desc = "When querying info 'jvm_thread_stack' or 'jvm_thread_detail', you can specify a thread id, multiple ids use ',' separate them"
            ),
            @JvmmOption(
                    name = "tdeep",
                    argName = "threadDepp",
                    order = 4,
                    desc = "When querying info 'jvm_thread_stack', this option is used to specify the stack depth, default 5"
            ),
            @JvmmOption(
                    name = "clazz",
                    argName = "class",
                    order = 4,
                    desc = "When querying info 'jvm_thread_pool'(required), this option is used to specify the class full path. " +
                            "JVMM will get instance of the thread pool through reflection, you need to use it with `loader`, `ifield` and `field` parameters"
            ),
            @JvmmOption(
                    name = "loader",
                    argName = "classloader",
                    order = 5,
                    desc = "When querying info 'jvm_thread_pool', this option is used to specify the classloader hashcode. " +
                            "You can execute `info -t jvm_classloader` to get classloader hashcode, if not filled, " +
                            "the default classloader will be used, You need to use it with `clazz`, `ifield` and `field` parameters"
            ),
            @JvmmOption(
                    name = "ifield",
                    argName = "instanceField",
                    order = 6,
                    desc = "When querying info 'jvm_thread_pool', this option is used to specify the instance field. " +
                            "If not filled, the static `field` of `clazz` will be read"
            ),
            @JvmmOption(
                    name = "field",
                    argName = "field",
                    order = 7,
                    desc = "When querying info 'jvm_thread_pool'(required), this option is used to specify the thread pool. " +
                            "If `ifield` is not filled, `field` will represent the static variable name of the thread " +
                            "pool stored in `clazz`, and if `ifeld` is filled in, `field` will represent the property " +
                            "variable name of the thread pool stored in the instance"
            )
    })
    @JvmmCmdDesc(
            headDesc = "Collect information about the target service.",
            tailDesc = "eg 1: `info -t process`\n" +
                    "eg 2: `info -t jvm_thread_pool -clazz org.beifengtz.jvmm.common.factory.ExecutorFactory -field SCHEDULE_THREAD_POOL\n" +
                    "eg 3: `info -t jvm_thread_detail -tid 2`\n" +
                    "eg 4: `info -t jvm_thread_stack -f thread_dump.txt`"
    )
    @Order(1)
    public static void info(JvmmConnector connector, CmdParser cmd) throws Exception {
        if (!cmd.hasArg("t")) {
            printErr("Missing required argument 't'");
            return;
        }
        CollectionType type;
        try {
            type = CollectionType.valueOf(cmd.getArg("t"));
        } catch (Exception e) {
            printErr("Invalid info type: " + cmd.getArg("t"));
            return;
        }

        JvmmRequest request = JvmmRequest.create();
        switch (type) {
            case process:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_PROCESS_INFO);
                break;
            case disk:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_DISK_INFO);
                break;
            case disk_io:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_DISK_IO_INFO);
                break;
            case cpu:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_CPU_INFO);
                break;
            case network:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_NETWORK_INFO);
                break;
            case sys:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_SYS_INFO);
                break;
            case sys_memory:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_SYS_MEMORY_INFO);
                break;
            case sys_file:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_SYS_FILE_INFO);
                break;
            case jvm_classloading:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_JVM_CLASSLOADING_INFO);
                break;
            case jvm_classloader:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_JVM_CLASSLOADER_INFO);
                break;
            case jvm_compilation:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_JVM_COMPILATION_INFO);
                break;
            case jvm_gc:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_JVM_GC_INFO);
                break;
            case jvm_memory:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_JVM_MEMORY_INFO);
                break;
            case jvm_memory_manager:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_JVM_MEMORY_MANAGER_INFO);
                break;
            case jvm_memory_pool:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_JVM_MEMORY_POOL_INFO);
                break;
            case jvm_thread:
                request.setType(GlobalType.JVMM_TYPE_COLLECT_JVM_THREAD_INFO);
                break;
            case jvm_thread_stack: {
                if (cmd.hasArg("tid")) {
                    request.setType(GlobalType.JVMM_TYPE_COLLECT_JVM_THREAD_STACK);
                    JsonObject data = new JsonObject();
                    if (cmd.hasArg("tdeep")) {
                        data.addProperty("depth", Integer.parseInt(cmd.getArg("tdeep")));
                    }
                    JsonArray idArr = new JsonArray();
                    String[] ids = cmd.getArg("tid").split(",");
                    for (String id : ids) {
                        idArr.add(Long.parseLong(id));
                    }
                    data.add("idArr", idArr);
                    request.setData(data);
                } else {
                    request.setType(GlobalType.JVMM_TYPE_COLLECT_JVM_DUMP_THREAD);
                }
                break;
            }
            case jvm_thread_detail: {
                request.setType(GlobalType.JVMM_TYPE_COLLECT_JVM_THREAD_DETAIL);
                if (cmd.hasArg("tid")) {
                    JsonArray idArr = new JsonArray();
                    String[] ids = cmd.getArg("tid").split(",");
                    for (String id : ids) {
                        idArr.add(Long.parseLong(id));
                    }
                    request.setData(idArr);
                }
                break;
            }
            case jvm_thread_pool: {
                request.setType(GlobalType.JVMM_TYPE_COLLECT_JVM_THREAD_POOL);
                String clazz = cmd.getArg("clazz");
                if (clazz == null) {
                    printErr("Missing required param `clazz`");
                    return;
                }
                String loader = cmd.getArg("loader");
                String ifield = cmd.getArg("ifield");
                String field = cmd.getArg("field");
                if (field == null) {
                    printErr("Missing required param `field`");
                    return;
                }
                JsonObject data = new JsonObject();
                data.addProperty("clazz", clazz);
                data.addProperty("field", field);
                if (loader != null) {
                    data.addProperty("classLoaderHash", Integer.parseInt(loader));
                }
                if (ifield != null) {
                    data.addProperty("instanceField", ifield);
                }
                request.setData(data);
                break;
            }
            default:
                printErr("Invalid info type: " + type);
                return;
        }

        JvmmResponse response = request(connector, request);
        if (response == null) {
            return;
        }
        if (cmd.hasArg("f")) {
            try {
                File file = new File(cmd.getArg("f"));
                String str;
                if (response.getData().isJsonArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonElement ele : response.getData().getAsJsonArray()) {
                        sb.append(ele.getAsString());
                    }
                    str = sb.toString();
                } else if (response.getData().isJsonObject()) {
                    Gson gson = new GsonBuilder()
                            .setPrettyPrinting()
                            .serializeNulls()
                            .create();
                    str = gson.toJson(response.getData());
                } else {
                    str = response.getData().toString();
                }
                FileUtil.writeByteArrayToFile(file, str.getBytes(StandardCharsets.UTF_8));
                System.out.println("Write server info to file successful, path is " + file.getAbsolutePath());
            } catch (IOException e) {
                printErr("Write failed, " + e.getMessage());
            }
        } else {
            if (Objects.equals(type, CollectionType.jvm_thread_stack)) {
                JsonArray stack = response.getData().getAsJsonArray();
                StringBuilder str = new StringBuilder();
                for (JsonElement ele : stack) {
                    str.append(ele.getAsString());
                }
                System.out.println(str);
            } else {
                System.out.println(StringUtil.formatJsonCode(response.getData().toString()));
            }
        }
    }

    @JvmmOptions({
            @JvmmOption(
                    name = "start",
                    order = 1,
                    desc = "To start a profiler task, you need to use it with `event`, `counter`, and `interval` parameters." +
                            " Only one profiler task can run at a time, to end the profiler task, use the `stop` parameter"
            ),
            @JvmmOption(
                    name = "stop",
                    order = 2,
                    desc = "Stop a running profiler task, you need to use it with `file` parameter."
            ),
            @JvmmOption(
                    name = "status",
                    order = 3,
                    desc = "View the current profiler status"
            ),
            @JvmmOption(
                    name = "list",
                    order = 4,
                    desc = "List supported events"
            ),
            @JvmmOption(
                    name = "f",
                    argName = "file",
                    order = 5,
                    desc = "Output file path, supported file type: html, txt, jfr. If not filled, will output text content"
            ),
            @JvmmOption(
                    name = "e",
                    argName = "event",
                    order = 6,
                    desc = "Sample event, default is cpu. Not all events are supported in the current environment, " +
                            "you can see which events are supported by the `list` parameter. " +
                            "Optional values: \n- cpu\n- alloc\n- lock\n- wall\n- itimer\n" +
                            "- `ClassName.javaMethodName`, instruments the given Java method in order to record all " +
                            "invocations of this method with the stack traces. Just for non-native methods, example: java.util.Properties.getProperty\n" +
                            "- `Java_nativeMethodName`, instruments the given native method in order to record all " +
                            "invocations of this method with the stack traces. Just for native methods, example: Java_java_lang_Throwable_fillInStackTrace"
            ),
            @JvmmOption(
                    name = "c",
                    argName = "counter",
                    order = 7,
                    desc = "Sample counter type, optional values: samples, total. Default value: samples."
            ),
            @JvmmOption(
                    name = "t",
                    argName = "time",
                    order = 8,
                    desc = "Sampling interval time, the unit is second. Default value: 10 s."
            ),
            @JvmmOption(
                    name = "i",
                    argName = "interval",
                    order = 9,
                    desc = "The time interval of the unit to collect samples, the unit is nanosecond, default: 10000000 ns."
            )
    })
    @JvmmCmdDesc(
            headDesc = "Get server sampling report. Only supported on MacOS and Linux.",
            tailDesc = "eg 1: `profiler -status`\n" +
                    "eg 2: `profiler -start -e wall`\n" +
                    "eg 3: `profiler -stop -f wall.html`\n" +
                    "eg 4: `profiler -list`\n" +
                    "eg 5: `profiler -e cpu -t 5`\n" +
                    "eg 6: `profiler -f java_method.html -e java.lang.Object.wait -t 20`\n" +
                    "eg 7: `profiler -f java_method.html -e Java_java_lang_Object_hashCode -t 20`"
    )
    @Order(2)
    public static void profiler(JvmmConnector connector, CmdParser cmd) {
        JvmmRequest request = JvmmRequest.create();
        boolean needArg = false;
        boolean responseForHex = false;
        if (cmd.hasArg("start")) {
            request.setType(GlobalType.JVMM_TYPE_PROFILER_SAMPLE_START);
            needArg = true;
        } else if (cmd.hasArg("stop")) {
            request.setType(GlobalType.JVMM_TYPE_PROFILER_SAMPLE_STOP);
            needArg = true;
            responseForHex = true;
        } else if (cmd.hasArg("status")) {
            request.setType(GlobalType.JVMM_TYPE_PROFILER_STATUS);
        } else if (cmd.hasArg("list")) {
            request.setType(GlobalType.JVMM_TYPE_PROFILER_LIST_EVENTS);
        } else {
            request.setType(GlobalType.JVMM_TYPE_PROFILER_SAMPLE);
            needArg = true;
            responseForHex = true;
        }

        long waitSecs = 20;
        String filePath = null;
        if (needArg) {
            JsonObject data = new JsonObject();
            if (cmd.hasArg("e")) {
                data.addProperty("event", cmd.getArg("e"));
            }

            if (cmd.hasArg("c")) {
                data.addProperty("counter", cmd.getArg("c"));
            }

            if (cmd.hasArg("t")) {
                long time = Long.parseLong(cmd.getArg("t"));
                data.addProperty("time", time);
                waitSecs = time + 10;
            }

            if (cmd.hasArg("i")) {
                data.addProperty("interval", Long.parseLong(cmd.getArg("i")));
            }

            if (cmd.hasArg("f")) {
                filePath = cmd.getArg("f");
                int dotIdx = filePath.lastIndexOf(".");
                if (dotIdx >= 0 && dotIdx < filePath.length() - 1) {
                    data.addProperty("format", filePath.substring(dotIdx + 1));
                }
            } else {
                data.addProperty("format", "txt");
            }

            request.setData(data);
        }

        JvmmResponse response = request(connector, request, waitSecs, TimeUnit.SECONDS);
        if (response == null) {
            return;
        }

        String content = response.getData().getAsString();
        byte[] bytes;
        if (responseForHex) {
            bytes = CodingUtil.hexStr2Bytes(content);
            content = new String(bytes, StandardCharsets.UTF_8);
        } else {
            bytes = content.getBytes(StandardCharsets.UTF_8);
        }

        try {
            if (filePath == null) {
                System.out.println(content);
            } else {
                File file = new File(filePath);
                FileUtil.writeByteArrayToFile(file, bytes);
                System.out.println("Write profiler to file successful, path is " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            printErr("Write failed, " + e.getMessage());
        }
    }


    @JvmmOptions({
            @JvmmOption(
                    name = "c",
                    argName = "class",
                    order = 1,
                    desc = "Required *. The java class to be decompiled"
            ),
            @JvmmOption(
                    name = "m",
                    argName = "method",
                    order = 2,
                    desc = "Specify the method name in the decompiled class"
            ),
            @JvmmOption(
                    name = "f",
                    argName = "file",
                    order = 3,
                    desc = "Output file name or path"
            ),
    })
    @Order(3)
    @JvmmCmdDesc(
            headDesc = "Decompile the class source code",
            tailDesc = "eg 1: `jad -c java.lang.String`\n" +
                    "eg 2: `jad -c java.lang.String -m equals`"
    )
    public static void jad(JvmmConnector connector, CmdParser cmd) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("className", cmd.getArg("c"));
        if (cmd.hasArg("m")) {
            json.addProperty("methodName", cmd.getArg("m"));
        }
        JvmmRequest request = JvmmRequest.create()
                .setType(GlobalType.JVMM_TYPE_EXECUTE_JAD)
                .setData(json);
        JvmmResponse response = request(connector, request);
        if (response == null) {
            return;
        }
        String result = response.getData().getAsString();
        if (cmd.hasArg("f")) {
            File file = new File(cmd.getArg("f"));
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileUtil.writeByteArrayToFile(file, result.getBytes(StandardCharsets.UTF_8));
            System.out.println("The decompilation result of the '" + cmd.getArg("c") + "' class has been output to the file " + file.getAbsolutePath());
        } else {
            System.out.println(result);
        }
    }

    @Order(4)
    @JvmmCmdDesc(headDesc = "View all java processes running on this physical machine.")
    public static void jps(JvmmConnector connector, CmdParser cmd) {
        JvmmRequest request = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_EXECUTE_JAVA_PROCESS);
        JvmmResponse response = request(connector, request);
        if (response == null) {
            return;
        }
        JsonArray processes = response.getData().getAsJsonArray();
        Gson gson = new Gson();
        for (JsonElement ele : processes) {
            JpsResult jps = gson.fromJson(ele, JpsResult.class);

            int prefixNum = String.valueOf(jps.getPid()).length() + jps.getMainClass().length();
            String prefix = StringUtil.repeat("", prefixNum);
            StringBuilder arguments = new StringBuilder();
            boolean firstLine = true;
            for (String argument : jps.getArguments()) {
                if (firstLine) {
                    arguments.append("\t").append(argument);
                    firstLine = false;
                } else {
                    arguments.append(prefix).append("\t\t\t").append(argument);
                }
                arguments.append("\n");
            }
            System.out.printf("%d\t%s\t%s", jps.getPid(), jps.getMainClass(), arguments);
        }
    }

    @Order(5)
    @IgnoreCmdParse
    @JvmmCmdDesc(
            headDesc = "Execute java tools(if these commands are supported on your machine). Usage: `jtool <jinfo|jstat|jstack|jamp|jcmd> [params]`"
    )
    public static void jtool(JvmmConnector connector, String command) {
        if (command.trim().length() == 0) {
            printErr("Can not execute empty command");
            return;
        }
        JvmmRequest request = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_EXECUTE_JVM_TOOL)
                .setData(new JsonPrimitive(command));
        JvmmResponse response = request(connector, request);
        if (response == null) {
            return;
        }
        JsonElement data = response.getData();
        if (data.isJsonArray()) {
            for (JsonElement ele : data.getAsJsonArray()) {
                System.out.println(ele.getAsString());
            }
        } else {
            System.out.println(response.getData().getAsString());
        }
    }

    @Order(6)
    @JvmmCmdDesc(headDesc = "Execute gc, no arguments.")
    public static void gc(JvmmConnector connector, CmdParser cmd) {
        JvmmRequest request = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_EXECUTE_GC);
        JvmmResponse response = request(connector, request);
        if (response == null) {
            return;
        }
        System.out.println("ok");
    }

    @Order(7)
    @JvmmOption(
            name = "t",
            argName = "type",
            desc = "Required *. The type of service to be closed, allowed values: \n- jvmm\n- http\n- sentinel"
    )
    @JvmmCmdDesc(
            headDesc = "Shutdown service.",
            tailDesc = "eg. `shutdown -t http`"
    )
    public static void shutdown(JvmmConnector connector, CmdParser cmd) {
        if (cmd.hasArg("t")) {
            JvmmRequest request = JvmmRequest.create()
                    .setType(GlobalType.JVMM_TYPE_SERVER_SHUTDOWN)
                    .setData(new JsonPrimitive(cmd.getArg("t")));
            JvmmResponse response = request(connector, request);
            if (response == null) {
                return;
            }
            System.out.println("ok");
        } else {
            printErr("Missing required param `t`");
        }
    }
}
