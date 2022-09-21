package org.beifengtz.jvmm.server.controller;

import com.google.gson.JsonPrimitive;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.JvmmController;
import org.beifengtz.jvmm.convey.annotation.JvmmMapping;
import org.beifengtz.jvmm.convey.annotation.RequestBody;
import org.beifengtz.jvmm.convey.annotation.RequestParam;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.enums.GlobalStatus;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.convey.enums.Method;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerCounter;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerEvent;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.entity.dto.ProfilerSampleDTO;

import java.io.File;
import java.io.IOException;
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
@HttpController
public class ProfilerController {

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_PROFILER_EXECUTE)
    @HttpRequest("/profiler/execute")
    public String execute(@RequestParam String command) throws IOException {
        return JvmmFactory.getProfiler().execute(command);
    }

    @JvmmMapping(typeEnum = GlobalType.JVMM_TYPE_PROFILER_SAMPLE)
    @HttpRequest(value = "/profiler/flame_graph", method = Method.POST)
    public JvmmResponse flameGraph(@RequestBody ProfilerSampleDTO data) throws Exception {

        File to = new File(JvmmFactory.getTempPath(), UUID.randomUUID() + "." + data.getFormat());
        if (to.getParentFile() != null && !to.getParentFile().exists()) {
            to.getParentFile().mkdirs();
        }

        ProfilerEvent event = data.getEvent();
        ProfilerCounter counter = data.getCounter();
        int time = data.getTime();

        Future<String> future;
        if (data.getInterval() != null) {
            future = JvmmFactory.getProfiler().sample(to, event, counter, data.getInterval(), time, TimeUnit.SECONDS);
        } else {
            future = JvmmFactory.getProfiler().sample(to, event, counter, time, TimeUnit.SECONDS);
        }
        JvmmResponse response = JvmmResponse.create().setType(GlobalType.JVMM_TYPE_PROFILER_SAMPLE.name());
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
