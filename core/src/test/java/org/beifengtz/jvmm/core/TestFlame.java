package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.common.util.PidUtil;
import org.beifengtz.jvmm.common.util.PlatformUtil;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerEvent;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
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
        URL script = getClass().getResource("/profiler.sh");
        if (script != null) {
            JvmmFactory.getExecutor().flameProfile((int) PidUtil.currentPid(), 20);
        }
    }

    @Test
    public void testProfiler() throws Exception {
        //  Github的测试环境执行没有权限，因此去掉
        if (PlatformUtil.isMac()) {
            JvmmProfiler profiler = JvmmFactory.getProfiler();
            Future<String> future = profiler.sample(new File("test_sample.html"), ProfilerEvent.cpu.name(), 5, TimeUnit.SECONDS);
            String s = future.get(10, TimeUnit.SECONDS);
            System.out.println(s);
        }
    }
}
