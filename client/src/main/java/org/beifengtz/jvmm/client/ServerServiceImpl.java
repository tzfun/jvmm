package org.beifengtz.jvmm.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.cli.CommandLine;
import org.beifengtz.jvmm.client.annotation.JvmmCmdDesc;
import org.beifengtz.jvmm.client.annotation.JvmmOption;
import org.beifengtz.jvmm.client.annotation.JvmmOptions;
import org.beifengtz.jvmm.common.util.CodingUtil;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.convey.GlobalType;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
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
                    desc = "Info type, optional values: system, systemDynamic, classloading, compilation, gc, process, " +
                            "memory, memoryManager, memoryPool, thread, threadStack."
            ),
            @JvmmOption(
                    name = "f",
                    hasArg = true,
                    argName = "output",
                    desc = "File path, output info to file."
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
            case "threadStack":
                request.setType(GlobalType.JVMM_TYPE_DUMP_THREAD_INFO);
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
                FileUtil.writeByteArrayToFile(file, response.getData().toString().getBytes(StandardCharsets.UTF_8));
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

    @JvmmCmdDesc(desc = "Shutdown jvmm server, no arguments.")
    public static void shutdown(JvmmConnector connector, CommandLine cmd) {
        JvmmRequest request = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_SERVER_SHUTDOWN);
        JvmmResponse response = request(connector, request);
        if (response == null) {
            return;
        }
        System.out.println("ok");
    }

    @JvmmCmdDesc(desc = "Shutdown jvmm server, no arguments.")
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
                    required = true,
                    hasArg = true,
                    argName = "file",
                    desc = "Output file path, file type indicates format type."
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

        long waitSecs = 12;
        if (cmd.hasOption("t")) {
            long time = Long.parseLong(cmd.getOptionValue("t"));
            data.addProperty("time", time);
            waitSecs = time + 2;
        }

        if (cmd.hasOption("i")) {
            data.addProperty("interval", Long.parseLong(cmd.getOptionValue("i")));
        }

        String filePath = cmd.getOptionValue("f");
        int dotIdx = filePath.lastIndexOf(".");
        if (dotIdx >= 0) {
            data.addProperty("format", filePath.substring(dotIdx));
        }

        request.setData(data);


        JvmmResponse response = request(connector, request, waitSecs, TimeUnit.SECONDS);
        if (response == null) {
            return;
        }

        String hexStr = response.getData().getAsString();
        byte[] bytes = CodingUtil.hexStr2Bytes(hexStr);
        try {
            File file = new File(filePath);
            FileUtil.writeByteArrayToFile(file, bytes);
            System.out.println("Write profiler to file successful, path is " + file.getAbsolutePath());
        } catch (IOException e) {
            printErr("Write failed, " + e.getMessage());
        }
    }
}
