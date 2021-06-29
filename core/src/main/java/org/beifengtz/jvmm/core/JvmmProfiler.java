package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.core.entity.profiler.ProfilerCommander;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerCounter;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerEvent;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 9:41 下午 2021/6/26
 *
 * @author beifengtz
 */
public interface JvmmProfiler {

    boolean isSystemSupported();

    String execute(String command) throws IOException;

    String execute(ProfilerCommander commander);

    String enabledEvents();

    String status();

    String version();

    /**
     * 采样数据并生成火焰图到文件
     *
     * @param executor 执行器，用于延迟结束profiler
     * @param to       生成文件
     * @param event    采样事件{@link ProfilerEvent}
     * @param counter  采样器类型{@link ProfilerCounter}
     * @param interval 采样间隔，单位纳秒ns
     * @param time     采样时间
     * @param timeUnit 采样时间单位
     * @return future
     */
    Future<String> sample(ScheduledExecutorService executor, File to, ProfilerEvent event, ProfilerCounter counter, long interval, long time, TimeUnit timeUnit);

    Future<String> sample(File to, ProfilerEvent event, ProfilerCounter counter, long interval, long time, TimeUnit timeUnit);

    Future<String> sample(ScheduledExecutorService executor, File to, ProfilerEvent event, ProfilerCounter counter, long time, TimeUnit timeUnit);

    Future<String> sample(File to, ProfilerEvent event, ProfilerCounter counter, long time, TimeUnit timeUnit);

    Future<String> sample(ScheduledExecutorService executor, File to, ProfilerEvent event, long time, TimeUnit timeUnit);

    Future<String> sample(File to, ProfilerEvent event, long time, TimeUnit timeUnit);

    Future<String> dumpCollapsed(ScheduledExecutorService executor, ProfilerCounter counter, ProfilerEvent event, long interval, long time, TimeUnit timeUnit);

    Future<String> dumpCollapsed(ScheduledExecutorService executor, ProfilerCounter counter, ProfilerEvent event, long time, TimeUnit timeUnit);

    Future<String> dumpCollapsed(ProfilerCounter counter, ProfilerEvent event, long interval, long time, TimeUnit timeUnit);

    Future<String> dumpCollapsed(ProfilerCounter counter, ProfilerEvent event, long time, TimeUnit timeUnit);

    Future<String> dumpTraces(ScheduledExecutorService executor, int maxTraces, ProfilerEvent event, long interval, long time, TimeUnit timeUnit);

    Future<String> dumpTraces(ScheduledExecutorService executor, int maxTraces, ProfilerEvent event, long time, TimeUnit timeUnit);

    Future<String> dumpTraces(int maxTraces, ProfilerEvent event, long interval, long time, TimeUnit timeUnit);

    Future<String> dumpTraces(int maxTraces, ProfilerEvent event, long time, TimeUnit timeUnit);

    Future<String> dumpFlat(ScheduledExecutorService executor, int maxMethods, ProfilerEvent event, long interval, long time, TimeUnit timeUnit);

    Future<String> dumpFlat(ScheduledExecutorService executor, int maxMethods, ProfilerEvent event, long time, TimeUnit timeUnit);

    Future<String> dumpFlat(int maxMethods, ProfilerEvent event, long interval, long time, TimeUnit timeUnit);

    Future<String> dumpFlat(int maxMethods, ProfilerEvent event, long time, TimeUnit timeUnit);
}
