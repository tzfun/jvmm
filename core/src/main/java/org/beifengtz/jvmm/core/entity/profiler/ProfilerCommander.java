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
    private String event;
    private ProfilerCounter counter;
    /**
     * 生成文件，文件名后缀指定了类型，例如：test、test.html、test.csv、test.jfr
     * <p>
     * 只有 *.jfr 是在 action 为{@link ProfilerAction#start}时生效，在stop时传入无效，其余的类型都是在 action 为{@link ProfilerAction#stop}时传入
     */
    private String file;
    private Long interval;
    private Integer traces;
    private Integer flat;
    /**
     * 如果生成的csv或html出现frame_buffer_overflow，需要扩大此值（默认值1_000_000）
     */
    private String frameBuf;
    private boolean threads;
    private boolean allUser = true;
    private boolean allKernel;
    private boolean fdtransfer;
    /**
     * 采样包含类，例如：org/beifengtz/*
     */
    private List<String> includes;
    /**
     * 排除采样类，例如：[java/*, demo/*]
     */
    private List<String> excludes;

    private ProfilerCommander() {
    }

    public static ProfilerCommander newInstance() {
        return new ProfilerCommander();
    }

    public String build() {
        if (this.action == null) {
            throw new IllegalArgumentException("Profiler commander missing required arguments: action");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.action.name()).append(",");
        boolean forJavaMethod = false;
        if (this.event != null) {
            forJavaMethod = event.indexOf(".") > 0;
            sb.append("event=").append(this.event).append(",");
        }
        if (this.counter != null) {
            sb.append("counter=").append(this.counter.name()).append(",");
        }
        if (this.file != null) {
            sb.append("file=").append(this.file).append(',');
        }
        if (!forJavaMethod && this.interval != null) {
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
            sb.append("threads,");
        }
        if (this.allKernel) {
            sb.append("allkernel,");
        }
        if (this.allUser) {
            sb.append("alluser,");
        }
        if (this.fdtransfer) {
            sb.append("fdtransfer,");
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

        if (sb.lastIndexOf(",") == sb.length() - 1) {
            sb.delete(sb.length() - 1, sb.length());
        }

        return sb.toString();
    }

    public ProfilerCommander setAction(ProfilerAction action) {
        this.action = action;
        return this;
    }

    public ProfilerCommander setEvent(String event) {
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

    public ProfilerCommander setFdtransfer(boolean fdtransfer) {
        this.fdtransfer = fdtransfer;
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

    public ProfilerCommander setInterval(Long interval) {
        this.interval = interval;
        return this;
    }

    public ProfilerCommander setTraces(Integer traces) {
        this.traces = traces;
        return this;
    }

    public ProfilerCommander setFlat(Integer flat) {
        this.flat = flat;
        return this;
    }

    public ProfilerAction getAction() {
        return action;
    }

    public String getEvent() {
        return event;
    }

    public ProfilerCounter getCounter() {
        return counter;
    }

    public String getFile() {
        return file;
    }

    public Long getInterval() {
        return interval;
    }

    public Integer getTraces() {
        return traces;
    }

    public Integer getFlat() {
        return flat;
    }

    public String getFrameBuf() {
        return frameBuf;
    }

    public boolean isThreads() {
        return threads;
    }

    public boolean isAllUser() {
        return allUser;
    }

    public boolean isAllKernel() {
        return allKernel;
    }

    public boolean isFdtransfer() {
        return fdtransfer;
    }

    public List<String> getIncludes() {
        return includes;
    }

    public List<String> getExcludes() {
        return excludes;
    }
}
