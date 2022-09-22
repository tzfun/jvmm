package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.common.util.PidUtil;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerEvent;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 9:48 下午 2021/6/22
 *
 * @author beifengtz
 */
public class TestFlame {

    @Test
    public void testFlameFile() throws Exception {
        JvmmFactory.getExecutor().flameProfile((int) PidUtil.currentPid(), 20);
    }

    @Test
    public void testProfiler() throws Exception {
        JvmmProfiler profiler = JvmmFactory.getProfiler();
        Future<String> future = profiler.sample(new File("test_sample.html"), ProfilerEvent.cpu.name(), 5, TimeUnit.SECONDS);
        String s = future.get(10, TimeUnit.SECONDS);
        System.out.println(s);
    }
}
