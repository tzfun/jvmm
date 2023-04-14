package org.beifengtz.jvmm.server.controller;

import com.google.gson.JsonPrimitive;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.common.util.meta.ListenableFuture;
import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.JvmmController;
import org.beifengtz.jvmm.convey.annotation.JvmmMapping;
import org.beifengtz.jvmm.convey.annotation.RequestBody;
import org.beifengtz.jvmm.convey.annotation.RequestParam;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.entity.ResponseFuture;
import org.beifengtz.jvmm.convey.enums.GlobalStatus;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.convey.enums.Method;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerCounter;
import org.beifengtz.jvmm.server.entity.dto.ProfilerSampleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 2021/6/29
 *
 * @author beifengtz
 */
@JvmmController
@HttpController
public class ProfilerController {

    private static final Logger logger = LoggerFactory.getLogger(ProfilerController.class);
    private static volatile boolean PROFILER_STARTED = false;

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_PROFILER_EXECUTE)
    @HttpRequest(value = "/profiler/execute", method = Method.POST)
    public String execute(@RequestBody String command) throws IOException {
        return JvmmFactory.getProfiler().execute(command);
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_PROFILER_SAMPLE)
    @HttpRequest(value = "/profiler/flame_graph", method = Method.POST)
    public void flameGraph(@RequestBody ProfilerSampleDTO data, ResponseFuture respFuture) {
        if (PROFILER_STARTED) {
            respFuture.apply(JvmmResponse.create()
                    .setType(GlobalType.JVMM_TYPE_PROFILER_SAMPLE)
                    .setStatus(GlobalStatus.JVMM_STATUS_PROFILER_FAILED)
                    .setMessage("Profiler started"));
            return;
        }

        File to = new File(FileUtil.getTempPath(), UUID.randomUUID() + "." + data.getFormat());
        if (to.getParentFile() != null && !to.getParentFile().exists()) {
            to.getParentFile().mkdirs();
        }

        String event = data.getEvent();
        ProfilerCounter counter = data.getCounter();
        int time = data.getTime();

        ListenableFuture<String> future;
        if (data.getInterval() != null) {
            future = JvmmFactory.getProfiler().sample(to, event, counter, data.getInterval(), time, TimeUnit.SECONDS);
        } else {
            future = JvmmFactory.getProfiler().sample(to, event, counter, time, TimeUnit.SECONDS);
        }
        PROFILER_STARTED = true;
        future.registerListener(f -> {
            PROFILER_STARTED = false;
            JvmmResponse response = JvmmResponse.create().setType(GlobalType.JVMM_TYPE_PROFILER_SAMPLE.name());

            if (f.isSuccess()) {
                String result = f.getNow();
                if (to.exists()) {
                    try {
                        response.setData(new JsonPrimitive(FileUtil.readToHexStr(to))).setStatus(GlobalStatus.JVMM_STATUS_OK);
                    } catch (IOException e) {
                        logger.error("Read profiler file failed: " + e.getMessage(), e);
                        response.setStatus(GlobalStatus.JVMM_STATUS_PROFILER_FAILED).setMessage(e.getMessage());
                    } finally {
                        to.delete();
                    }
                } else {
                    response.setStatus(GlobalStatus.JVMM_STATUS_PROFILER_FAILED).setMessage("Generate failed");
                }
                response.setMessage(result);
            } else {
                response.setStatus(GlobalStatus.JVMM_STATUS_PROFILER_FAILED).setMessage(f.getCause().getMessage());
            }
            respFuture.apply(response);
        });
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_PROFILER_SAMPLE_START)
    @HttpRequest(value = "/profiler/start", method = Method.POST)
    public String start(@RequestBody ProfilerSampleDTO data) {
        if (PROFILER_STARTED) {
            return "Profiler started";
        }
        String result = JvmmFactory.getProfiler().start(data.getEvent(), data.getCounter(), data.getInterval());
        PROFILER_STARTED = true;
        return result;
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_PROFILER_SAMPLE_STOP)
    @HttpRequest(value = "/profiler/stop", method = Method.POST)
    public JvmmResponse stop(@RequestParam String format) throws IOException {
        JvmmResponse response = JvmmResponse.create().setType(GlobalType.JVMM_TYPE_PROFILER_SAMPLE_STOP.name());
        if (!PROFILER_STARTED) {
            return response.setStatus(GlobalStatus.JVMM_STATUS_PROFILER_FAILED).setMessage("Profiler not start");
        }
        if (format == null || format.isEmpty()) {
            format = "html";
        }
        File to = new File(FileUtil.getTempPath(), UUID.randomUUID() + "." + format);
        if (to.getParentFile() != null && !to.getParentFile().exists()) {
            to.getParentFile().mkdirs();
        }
        try {
            JvmmFactory.getProfiler().stop(to);
            if (to.exists()) {
                response.setData(new JsonPrimitive(FileUtil.readToHexStr(to))).setStatus(GlobalStatus.JVMM_STATUS_OK);
                to.delete();
            } else {
                response.setStatus(GlobalStatus.JVMM_STATUS_PROFILER_FAILED).setMessage("Generate failed");
            }
            return response;
        } finally {
            PROFILER_STARTED = false;
            if (to.exists()) {
                to.delete();
            }
        }
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_PROFILER_STATUS)
    @HttpRequest(value = "/profiler/status")
    public String status() {
        return JvmmFactory.getProfiler().status();
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_PROFILER_LIST_EVENTS)
    @HttpRequest(value = "/profiler/list_events")
    public String listEvents() {
        return JvmmFactory.getProfiler().enabledEvents();
    }
}
