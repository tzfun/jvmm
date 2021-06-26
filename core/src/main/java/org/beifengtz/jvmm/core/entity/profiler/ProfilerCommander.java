package org.beifengtz.jvmm.core.entity.profiler;

import java.util.List;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 5:18 下午 2021/6/26
 *
 * @author beifengtz
 */
public class ProfilerCommander {

    private ProfilerAction action;
    private ProfilerEvent event;
    private ProfilerCounter counter;
    private String file;
    private Long interval;
    private Integer traces;
    private Integer flat;
    private String frameBuf;
    private boolean threads;
    private boolean allUser;
    private boolean allKernel;
    private List<String> includes;
    private List<String> excludes;

    private ProfilerCommander() {
    }

    public static ProfilerCommander newInstance() {
        return new ProfilerCommander();
    }

    public String build() {
        if (this.action == null) {
            throw new IllegalArgumentException("Missing required arguments: action");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.action.name()).append(",");
        if (this.event != null) {
            sb.append(this.event.name()).append(",");
        }
        if (this.counter != null) {
            sb.append("counter=").append(this.counter.name()).append(",");
        }
        if (this.file != null) {
            sb.append("file=").append(this.file).append(',');
        }
        if (this.interval != null) {
            sb.append("interval=").append(this.interval).append(',');
        }
        if (this.traces != null) {
            sb.append("traces=").append(this.traces).append(',');
        }
        if (this.flat != null) {
            sb.append("flat=").append(this.flat).append(',');
        }
        if (this.frameBuf != null) {
            sb.append("framebuf=").append(this.frameBuf).append(',');
        }
        if (this.threads) {
            sb.append("threads").append(',');
        }
        if (this.allKernel) {
            sb.append("allkernel").append(',');
        }
        if (this.allUser) {
            sb.append("alluser").append(',');
        }
        if (this.includes != null) {
            for (String include : includes) {
                sb.append("include=").append(include).append(',');
            }
        }
        if (this.excludes != null) {
            for (String exclude : excludes) {
                sb.append("exclude=").append(exclude).append(',');
            }
        }

        return sb.toString();
    }

    public ProfilerCommander setAction(ProfilerAction action) {
        this.action = action;
        return this;
    }

    public ProfilerCommander setEvent(ProfilerEvent event) {
        this.event = event;
        return this;
    }

    public ProfilerCommander setCounter(ProfilerCounter counter) {
        this.counter = counter;
        return this;
    }

    public ProfilerCommander setFile(String file) {
        this.file = file;
        return this;
    }

    public ProfilerCommander setInterval(long interval) {
        this.interval = interval;
        return this;
    }

    public ProfilerCommander setTraces(int traces) {
        this.traces = traces;
        return this;
    }

    public ProfilerCommander setFlat(int flat) {
        this.flat = flat;
        return this;
    }

    public ProfilerCommander setFrameBuf(String frameBuf) {
        this.frameBuf = frameBuf;
        return this;
    }

    public ProfilerCommander setThreads(boolean threads) {
        this.threads = threads;
        return this;
    }

    public ProfilerCommander setAllUser(boolean allUser) {
        this.allUser = allUser;
        return this;
    }

    public ProfilerCommander setAllKernel(boolean allKernel) {
        this.allKernel = allKernel;
        return this;
    }

    public ProfilerCommander setIncludes(List<String> includes) {
        this.includes = includes;
        return this;
    }

    public ProfilerCommander setExcludes(List<String> excludes) {
        this.excludes = excludes;
        return this;
    }
}
