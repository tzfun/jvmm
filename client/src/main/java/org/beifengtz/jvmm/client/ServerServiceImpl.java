package org.beifengtz.jvmm.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.commons.cli.CommandLine;
import org.beifengtz.jvmm.client.annotation.JvmmOption;
import org.beifengtz.jvmm.client.annotation.JvmmOptions;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.convey.GlobalStatus;
import org.beifengtz.jvmm.convey.GlobalType;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
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
                    desc = "Get node information, optional values: system, systemDynamic, classloading, compilation, gc, process, " +
                            "memory, memoryManager, memoryPool, thread, threadStack"
            )
    })
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
                System.out.println("Invalid info type: " + type);
                return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        connector.registerListener(response -> {
            if (response.getType().equals(request.getType())) {
                latch.countDown();
                if (Objects.equals(response.getStatus(), GlobalStatus.JVMM_STATUS_OK.name())) {
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

                } else {
                    System.err.printf("Wrong response status: '%s', msg: %s%n", response.getStatus(), response.getMessage());
                }
            }
        });

        connector.send(request);

        if (!latch.await(3, TimeUnit.SECONDS)) {
            System.err.println("Request time out");
        }
    }

    @JvmmOptions({
            @JvmmOption(name = "a", hasArg = true, argName = "agent", desc = "")
    })
    public static void gc(JvmmConnector connector, CommandLine cmd) {

    }
}
