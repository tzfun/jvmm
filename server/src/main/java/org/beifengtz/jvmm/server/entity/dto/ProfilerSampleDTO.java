package org.beifengtz.jvmm.server.entity.dto;

import org.beifengtz.jvmm.core.entity.profiler.ProfilerCounter;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerEvent;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 17:38 2022/9/13
 *
 * @author beifengtz
 */
public class ProfilerSampleDTO{
    private String format = "html";
    private String event = ProfilerEvent.cpu.name();
    private ProfilerCounter counter = ProfilerCounter.samples;
    private int time =  10; //  单位秒
    private Long interval;

    public String getFormat() {
        return format;
    }

    public String getEvent() {
        return event;
    }

    public ProfilerCounter getCounter() {
        return counter;
    }

    public int getTime() {
        return time;
    }

    public ProfilerSampleDTO setFormat(String format) {
        this.format = format;
        return this;
    }

    public ProfilerSampleDTO setEvent(String event) {
        this.event = event;
        return this;
    }

    public ProfilerSampleDTO setCounter(ProfilerCounter counter) {
        this.counter = counter;
        return this;
    }

    public ProfilerSampleDTO setTime(int time) {
        this.time = time;
        return this;
    }

    public Long getInterval() {
        return interval;
    }

    public ProfilerSampleDTO setInterval(Long interval) {
        this.interval = interval;
        return this;
    }
}
