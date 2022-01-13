package org.beifengtz.jvmm.server.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.util.concurrent.EventExecutor;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.common.util.SystemPropertyUtil;
import org.beifengtz.jvmm.convey.GlobalStatus;
import org.beifengtz.jvmm.convey.GlobalType;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerCounter;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerEvent;
import org.beifengtz.jvmm.server.ServerConfig;
import org.beifengtz.jvmm.server.annotation.JvmmController;
import org.beifengtz.jvmm.server.annotation.JvmmMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Future;
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
public class ProfilerController {

    private static final Logger log = LoggerFactory.getLogger(ProfilerController.class);

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_PROFILER_EXECUTE)
    public String execute(JsonElement data) throws IOException {
        return JvmmFactory.getProfiler().execute(data.getAsString());
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_PROFILER_SAMPLE)
    public JvmmResponse sample(String type, EventExecutor executor, JsonObject data) throws Exception {

        String format;
        if (data.has("format")) {
            format = data.get("format").getAsString();
        } else {
            format = "html";
        }

        File to = new File(ServerConfig.getTempPath(), UUID.randomUUID() + "." + format);
        if (to.getParentFile() != null && !to.getParentFile().exists()) {
            to.getParentFile().mkdirs();
        }

        ProfilerEvent event;
        if (data.has("event")) {
            event = ProfilerEvent.valueOf(data.get("event").getAsString().toLowerCase(Locale.ROOT));
        } else {
            event = ProfilerEvent.cpu;
        }

        ProfilerCounter counter;
        if (data.has("counter")) {
            counter = ProfilerCounter.valueOf(data.get("counter").getAsString().toLowerCase(Locale.ROOT));
        } else {
            counter = ProfilerCounter.samples;
        }

        //  单位秒
        int time = 10;
        if (data.has("time")) {
            time = data.get("time").getAsInt();
        }

        Future<String> future;
        if (data.has("interval")) {
            future = JvmmFactory.getProfiler().sample(to, event, counter, data.get("interval").getAsLong(), time, TimeUnit.SECONDS);
        } else {
            future = JvmmFactory.getProfiler().sample(to, event, counter, time, TimeUnit.SECONDS);
        }
        JvmmResponse response = JvmmResponse.create().setType(type);
        String result = future.get();
        if (to.exists()) {
            String hexStr = FileUtil.readToHexStr(to);
            response.setStatus(GlobalStatus.JVMM_STATUS_OK).setData(new JsonPrimitive(hexStr));
            to.delete();
        } else {
            response.setStatus(GlobalStatus.JVMM_STATUS_PROFILER_FAILED);
        }
        response.setMessage(result);

        return response;
    }
}
