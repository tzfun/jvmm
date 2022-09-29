package org.beifengtz.jvmm.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.cli.CommandLine;
import org.beifengtz.jvmm.client.annotation.IgnoreCmdParse;
import org.beifengtz.jvmm.client.annotation.JvmmCmdDesc;
import org.beifengtz.jvmm.client.annotation.JvmmOption;
import org.beifengtz.jvmm.client.annotation.JvmmOptions;
import org.beifengtz.jvmm.common.util.CodingUtil;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.beifengtz.jvmm.core.entity.result.JpsResult;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 4:07 下午 2021/12/12
 *
 * @author beifengtz
 */
public class ServerServiceImpl extends ServerService {

    @JvmmOptions({
            @JvmmOption(
                    name = "t",
                    hasArg = true,
                    required = true,
                    argName = "type",
                    desc = "Info type (required *), optional values: system, systemDynamic, classloading, classloader, compilation, gc, process, " +
                            "memory, memoryManager, memoryPool, thread, threadStack."
            ),
            @JvmmOption(
                    name = "f",
                    hasArg = true,
                    argName = "output",
                    desc = "File path (optional), output info to file."
            ),
            @JvmmOption(
                    name = "tid",
                    hasArg = true,
                    argName = "threadId",
                    desc = "When querying info 'threadStack', you can specify a thread id, multiple ids use ',' separate them"
            ),
            @JvmmOption(
                    name = "tdeep",
                    hasArg = true,
                    argName = "threadDepp",
                    desc = "When querying info 'threadStack', this option is used to specify the stack depth, default 5"
            )
    })
    @JvmmCmdDesc(desc = "Get information about the target server")
    public static void info(JvmmConnector connector, CommandLine cmd) throws Exception {
        String type = cmd.getOptionValue("t");

        JvmmRequest request = JvmmRequest.create();
        switch (type) {
            case "system":
                request.setType(GlobalType.JVMM_TYPE_COLLECT_SYSTEM_STATIC_INFO);
                break;
            case "systemDynamic":
                request.setType(GlobalType.JVMM_TYPE_COLLECT_SYSTEM_DYNAMIC_INFO);
                break;
            case "classloading":
                request.setType(GlobalType.JVMM_TYPE_COLLECT_CLASSLOADING_INFO);
                break;
            case "classloader":
                request.setType(GlobalType.JVMM_TYPE_COLLECT_CLASSLOADER_INFO);
                break;
            case "compilation":
                request.setType(GlobalType.JVMM_TYPE_COLLECT_COMPILATION_INFO);
                break;
            case "gc":
                request.setType(GlobalType.JVMM_TYPE_COLLECT_GARBAGE_COLLECTOR_INFO);
                break;
            case "process":
                request.setType(GlobalType.JVMM_TYPE_COLLECT_PROCESS_INFO);
                break;
            case "memory":
                request.setType(GlobalType.JVMM_TYPE_COLLECT_MEMORY_INFO);
                break;
            case "memoryManager":
                request.setType(GlobalType.JVMM_TYPE_COLLECT_MEMORY_MANAGER_INFO);
                break;
            case "memoryPool":
                request.setType(GlobalType.JVMM_TYPE_COLLECT_MEMORY_POOL_INFO);
                break;
            case "thread":
                request.setType(GlobalType.JVMM_TYPE_COLLECT_THREAD_DYNAMIC_INFO);
                break;
            case "threadStack": {
                if (cmd.hasOption("tid")) {
                    request.setType(GlobalType.JVMM_TYPE_COLLECT_THREAD_INFO);
                    JsonObject data = new JsonObject();
                    if (cmd.hasOption("tdeep")) {
                        data.addProperty("depth", Integer.parseInt(cmd.getOptionValue("tdeep")));
                    }
                    JsonArray idArr = new JsonArray();
                    String[] ids = cmd.getOptionValue("tid").split(",");
                    for (String id : ids) {
                        idArr.add(Long.parseLong(id));
                    }
                    data.add("idArr", idArr);
                    request.setData(data);
                } else {
                    request.setType(GlobalType.JVMM_TYPE_DUMP_THREAD_INFO);
                }
            }
            break;
            default:
                printErr("Invalid info type: " + type);
                return;
        }

        JvmmResponse response = request(connector, request);
        if (response == null) {
            return;
        }
        if (cmd.hasOption("f")) {
            try {
                File file = new File(cmd.getOptionValue("f"));
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
            if (Objects.equals(type, "threadStack")) {
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

    @JvmmCmdDesc(desc = "Execute gc, no arguments.")
    public static void gc(JvmmConnector connector, CommandLine cmd) {
        JvmmRequest request = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_EXECUTE_GC);
        JvmmResponse response = request(connector, request);
        if (response == null) {
            return;
        }
        System.out.println("ok");
    }

    @JvmmOption(
            name = "t",
            required = true,
            hasArg = true,
            argName = "type",
            desc = "The type of service to be closed, allowed values: jvmm, http, sentinel"
    )
    @JvmmCmdDesc(desc = "Shutdown service.")
    public static void shutdown(JvmmConnector connector, CommandLine cmd) {
        JvmmRequest request = JvmmRequest.create()
                .setType(GlobalType.JVMM_TYPE_SERVER_SHUTDOWN)
                .setData(new JsonPrimitive(cmd.getOptionValue("t")));
        JvmmResponse response = request(connector, request);
        if (response == null) {
            return;
        }
        System.out.println("ok");
    }

    @JvmmCmdDesc(desc = "View all java processes running on this physical machine.")
    public static void jps(JvmmConnector connector, CommandLine cmd) {
        JvmmRequest request = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_EXECUTE_JAVA_PROCESS);
        JvmmResponse response = request(connector, request);
        if (response == null) {
            return;
        }
        JsonArray processes = response.getData().getAsJsonArray();
        Gson gson = new Gson();
        for (JsonElement ele : processes) {
            JpsResult jps = gson.fromJson(ele, JpsResult.class);
            System.out.printf("%d\t%s\t%s\n", jps.getPid(), jps.getMainClass(), jps.getArguments());
        }
    }

    @JvmmOptions({
            @JvmmOption(
                    name = "e",
                    hasArg = true,
                    argName = "event",
                    desc = "Sample event, optional values: cpu, alloc, lock, wall, itimer. Default value: cpu."
            ),
            @JvmmOption(
                    name = "c",
                    hasArg = true,
                    argName = "counter",
                    desc = "Sample counter type, optional values: samples, total. Default value: samples."
            ),
            @JvmmOption(
                    name = "t",
                    hasArg = true,
                    argName = "time",
                    desc = "Sampling interval time, the unit is second. Default value: 10 s."
            ),
            @JvmmOption(
                    name = "i",
                    hasArg = true,
                    argName = "interval",
                    desc = "The time interval of the unit to collect samples, the unit is nanosecond. Default value: 10000000 ns."
            ),
            @JvmmOption(
                    name = "f",
                    hasArg = true,
                    argName = "file",
                    desc = "Output file path, supported file type: html, txt, jfr. If not filled, will output text content"
            )
    })
    @JvmmCmdDesc(desc = "Get server sampling report. Only supported on MacOS and Linux.")
    public static void profiler(JvmmConnector connector, CommandLine cmd) {
        JvmmRequest request = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_PROFILER_SAMPLE);
        JsonObject data = new JsonObject();
        if (cmd.hasOption("e")) {
            data.addProperty("event", cmd.getOptionValue("e"));
        }

        if (cmd.hasOption("c")) {
            data.addProperty("counter", cmd.getOptionValue("c"));
        }

        long waitSecs = 20;
        if (cmd.hasOption("t")) {
            long time = Long.parseLong(cmd.getOptionValue("t"));
            data.addProperty("time", time);
            waitSecs = time + 10;
        }

        if (cmd.hasOption("i")) {
            data.addProperty("interval", Long.parseLong(cmd.getOptionValue("i")));
        }

        String filePath = null;
        if (cmd.hasOption("f")) {
            filePath = cmd.getOptionValue("f");
            int dotIdx = filePath.lastIndexOf(".");
            if (dotIdx >= 0 && dotIdx < filePath.length() - 1) {
                data.addProperty("format", filePath.substring(dotIdx + 1));
            }
        } else {
            data.addProperty("format", "txt");
        }

        request.setData(data);


        JvmmResponse response = request(connector, request, waitSecs, TimeUnit.SECONDS);
        if (response == null) {
            return;
        }

        String hexStr = response.getData().getAsString();
        byte[] bytes = CodingUtil.hexStr2Bytes(hexStr);
        try {
            if (filePath == null) {
                System.out.println(new String(bytes, StandardCharsets.UTF_8));
            } else {
                File file = new File(filePath);
                FileUtil.writeByteArrayToFile(file, bytes);
                System.out.println("Write profiler to file successful, path is " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            printErr("Write failed, " + e.getMessage());
        }
    }

    @IgnoreCmdParse
    @JvmmCmdDesc(desc = "Execute java tools(if these commands are supported on your machine). jtool <jps|jinfo|jstat|jstack|jamp|jcmd> [params...]")
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

    @JvmmOptions({
            @JvmmOption(
                    name = "c",
                    required = true,
                    hasArg = true,
                    argName = "class",
                    desc = "Required, the java class to be decompiled"
            ),
            @JvmmOption(
                    name = "m",
                    hasArg = true,
                    argName = "method",
                    desc = "Specify the method name in the decompiled class"
            ),
            @JvmmOption(
                    name = "f",
                    hasArg = true,
                    argName = "file",
                    desc = "Output file path"
            ),
    })

    @JvmmCmdDesc(desc = "Shutdown service.")
    public static void jad(JvmmConnector connector, CommandLine cmd) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("className", cmd.getOptionValue("c"));
        if (cmd.hasOption("m")) {
            json.addProperty("methodName", cmd.getOptionValue("m"));
        }
        JvmmRequest request = JvmmRequest.create()
                .setType(GlobalType.JVMM_TYPE_EXECUTE_JAD)
                .setData(json);
        JvmmResponse response = request(connector, request);
        if (response == null) {
            return;
        }
        String result = response.getData().getAsString();
        if (cmd.hasOption("f")) {
            File file = new File(cmd.getOptionValue("f"));
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileUtil.writeByteArrayToFile(file, result.getBytes(StandardCharsets.UTF_8));
            System.out.println("The decompilation result of the '" + cmd.getOptionValue("c") + "' class has been output to the file " + file.getAbsolutePath());
        } else {
            System.out.println(result);
        }
    }
}
