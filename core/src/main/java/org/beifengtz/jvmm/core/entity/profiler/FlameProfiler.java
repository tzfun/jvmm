package org.beifengtz.jvmm.core.entity.profiler;

import org.beifengtz.jvmm.tools.util.PlatformUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 6:28 下午 2021/6/23
 *
 * @author beifengtz
 */
public class FlameProfiler {

    private static final Logger log = LoggerFactory.getLogger(FlameProfiler.class);

    private static FlameProfiler instance = null;

    private static String libFilePath;

    static {
        String soPath = null;

        if (PlatformUtil.isMac()) {
            if (!PlatformUtil.isArm64()) {
                soPath = "/async-profiler/libasyncProfiler-macos-x64.so";
            }
        } else if (PlatformUtil.isLinux()) {
            if (PlatformUtil.isArm32()) {
                soPath = "/async-profiler/libasyncProfiler-linux-arm.so";
            } else if (PlatformUtil.isArm64()) {
                soPath = "/async-profiler/libasyncProfiler-linux-aarch64.so";
            } else if (PlatformUtil.isX86()) {
                soPath = "/async-profiler/libasyncProfiler-linux-x86.so";
            } else {
                soPath = "/async-profiler/libasyncProfiler-linux-x64.so";
            }
        }

        if (soPath != null) {
            URL resource = FlameProfiler.class.getResource(soPath);
            if (resource != null){
                libFilePath = resource.getFile();
            }
        }
    }

    private FlameProfiler(){
    }

    public static synchronized FlameProfiler getInstance() {
        return getInstance(null);
    }

    public static synchronized FlameProfiler getInstance(String soFilePath) {

        if (instance != null){
            return instance;
        }

        if (soFilePath != null){
            libFilePath = soFilePath;
        }

        if (libFilePath == null){
            throw new IllegalStateException("Can not found libasyncProfiler.so, Only support Linux & Mac.");
        }

        System.load(libFilePath);
        instance = new FlameProfiler();
        return instance;
    }

    public void start(String event, long interval) throws IllegalStateException {
        start0(event, interval, true);
    }

    public void stop() throws IllegalStateException {
        stop0();
    }

    public String execute(String command) throws IllegalArgumentException, IOException {
        return execute0(command);
    }

    public String dumpCollapsed(Counter counter) {
        try {
            return execute0("collapsed,counter=" + counter.name().toLowerCase());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String dumpTraces(int maxTraces) {
        try {
            return execute0("summary,traces=" + maxTraces);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String dumpFlat(int maxMethods) {
        try {
            return execute0("summary,flat=" + maxMethods);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addThread(Thread thread) {
        filterThread(thread, true);
    }

    public void removeThread(Thread thread) {
        filterThread(thread, false);
    }

    private void filterThread(Thread thread, boolean enable) {
        if (thread == null) {
            filterThread0(null, enable);
        } else {
            synchronized (thread) {
                Thread.State state = thread.getState();
                if (state != Thread.State.NEW && state != Thread.State.TERMINATED) {
                    filterThread0(thread, enable);
                }
            }
        }
    }

    public native long getSamples();
    private native void start0(String event, long interval, boolean reset) throws IllegalStateException;
    private native void stop0() throws IllegalStateException;
    private native String execute0(String command) throws IllegalArgumentException, IOException;
    private native void filterThread0(Thread thread, boolean enable);

    public static void main(String[] args) throws Exception{
        FlameProfiler profiler = getInstance();
        String version = profiler.execute("version");
        System.out.println(version);
        String result = profiler.execute("start,threads,alluser,allkernel,event=cpu");
        System.out.println(result);
//        result = profiler.dumpCollapsed(Counter.SAMPLES);
        System.out.println(result);
        System.out.println(profiler.getSamples());
        Thread.sleep(10000);
        result = profiler.execute("stop,file=/Users/beifengtz/Program/jvmm/core/src/test/test.svg,");
        System.out.println(result);
    }
}
