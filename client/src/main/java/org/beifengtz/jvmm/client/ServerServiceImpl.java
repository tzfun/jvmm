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
                    desc = "Required info type, optional values: \n\t- process\n\t- disk\n\t- disk_io\n\t- cpu" +
                            "\n\t- network\n\t- sys\n\t- sys_memory\n\t- sys_file\n\t- jvm_classloading\n\t- jvm_classloader" +
                            "\n\t- jvm_compilation\n\t- jvm_gc\n\t- jvm_memory\n\t- jvm_memory_manager\n\t- jvm_memory_pool" +
                            "\n\t- jvm_thread\n\t- jvm_thread_stack\n\t- jvm_thread_detail\n\t- jvm_thread_pool"
            ),
            @JvmmOption(
                    name = "f",
                    argName = "output",
                    desc = "File path (optional), output info to file."
            ),
            @JvmmOption(
                    name = "tid",
                    argName = "threadId",
                    desc = "When querying info 'jvm_thread_stack' or 'jvm_thread_detail', you can specify a thread id, multiple ids use ',' separate them"
            ),
            @JvmmOption(
                    name = "tdeep",
                    argName = "threadDepp",
                    desc = "When querying info 'jvm_thread_stack', this option is used to specify the stack depth, default 5"
            ),
            @JvmmOption(
                    name = "clazz",
                    argName = "class",
                    desc = "When querying info 'jvm_thread_pool'(required), this option is used to specify the class full path. " +
                            "JVMM will get instance of the thread pool through reflection, you need to use it with `loader`, `ifield` and `field` parameters"
            ),
            @JvmmOption(
                    name = "loader",
                    argName = "classloader",
                    desc = "When querying info 'jvm_thread_pool', this option is used to specify the classloader hashcode. " +
                            "You can execute `info -t jvm_classloader` to get classloader hashcode, if not filled, " +
                            "the default classloader will be used, You need to use it with `clazz`, `ifield` and `field` parameters"
            ),
            @JvmmOption(
                    name = "ifield",
                    argName = "instanceField",
                    desc = "When querying info 'jvm_thread_pool', this option is used to specify the instance field. " +
                            "If not filled, the static `field` of `clazz` will be read"
            ),
            @JvmmOption(
                    name = "field",
                    argName = "field",
                    desc = "When querying info 'jvm_thread_pool'(required), this option is used to specify the thread pool. " +
                            "If `ifield` is not filled, `field` will represent the static variable name of the thread " +
                            "pool stored in `clazz`, and if `ifeld` is filled in, `field` will represent the property " +
                            "variable name of the thread pool stored in the instance"
            )
    })
    @JvmmCmdDesc(headDesc =  "Get information about the target server. \neg. info -t process")
    public static void info(JvmmConnector connector, CmdParser cmd) throws Exception {
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

    @JvmmCmdDesc(headDesc =  "Execute gc, no arguments.")
    public static void gc(JvmmConnector connector, CmdParser cmd) {
        JvmmRequest request = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_EXECUTE_GC);
        JvmmResponse response = request(connector, request);
        if (response == null) {
            return;
        }
        System.out.println("ok");
    }

    @JvmmOption(
            name = "t",
            argName = "type",
            desc = "Required *. The type of service to be closed, allowed values: jvmm, http, sentinel"
    )
    @JvmmCmdDesc(headDesc =  "Shutdown service.")
    public static void shutdown(JvmmConnector connector, CmdParser cmd) {
        JvmmRequest request = JvmmRequest.create()
                .setType(GlobalType.JVMM_TYPE_SERVER_SHUTDOWN)
                .setData(new JsonPrimitive(cmd.getArg("t")));
        JvmmResponse response = request(connector, request);
        if (response == null) {
            return;
        }
        System.out.println("ok");
    }

    @JvmmCmdDesc(headDesc =  "View all java processes running on this physical machine.")
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
            System.out.printf("%d\t%s\t%s\n", jps.getPid(), jps.getMainClass(), jps.getArguments());
        }
    }

    @JvmmOptions({
            @JvmmOption(
                    name = "e",
                    argName = "event",
                    desc = "Sample event, optional values: cpu, alloc, lock, wall, itimer. Default value: cpu."
            ),
            @JvmmOption(
                    name = "c",
                    argName = "counter",
                    desc = "Sample counter type, optional values: samples, total. Default value: samples."
            ),
            @JvmmOption(
                    name = "t",
                    argName = "time",
                    desc = "Sampling interval time, the unit is second. Default value: 10 s."
            ),
            @JvmmOption(
                    name = "i",
                    argName = "interval",
                    desc = "The time interval of the unit to collect samples, the unit is nanosecond. Default value: 10000000 ns."
            ),
            @JvmmOption(
                    name = "f",
                    argName = "file",
                    desc = "Output file path, supported file type: html, txt, jfr. If not filled, will output text content"
            )
    })
    @JvmmCmdDesc(headDesc =  "Get server sampling report. Only supported on MacOS and Linux.")
    public static void profiler(JvmmConnector connector, CmdParser cmd) {
        JvmmRequest request = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_PROFILER_SAMPLE);
        JsonObject data = new JsonObject();
        if (cmd.hasArg("e")) {
            data.addProperty("event", cmd.getArg("e"));
        }

        if (cmd.hasArg("c")) {
            data.addProperty("counter", cmd.getArg("c"));
        }

        long waitSecs = 20;
        if (cmd.hasArg("t")) {
            long time = Long.parseLong(cmd.getArg("t"));
            data.addProperty("time", time);
            waitSecs = time + 10;
        }

        if (cmd.hasArg("i")) {
            data.addProperty("interval", Long.parseLong(cmd.getArg("i")));
        }

        String filePath = null;
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
    @JvmmCmdDesc(headDesc =  "Execute java tools(if these commands are supported on your machine). jtool <jps|jinfo|jstat|jstack|jamp|jcmd> [params...]")
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
                    argName = "class",
                    desc = "Required *. The java class to be decompiled"
            ),
            @JvmmOption(
                    name = "m",
                    argName = "method",
                    desc = "Specify the method name in the decompiled class"
            ),
            @JvmmOption(
                    name = "f",
                    argName = "file",
                    desc = "Output file path"
            ),
    })
    @JvmmCmdDesc(headDesc =  "Decompile class files at runtime.")
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
}
