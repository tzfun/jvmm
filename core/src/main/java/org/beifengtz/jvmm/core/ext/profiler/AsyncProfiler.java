package org.beifengtz.jvmm.core.ext.profiler;

import org.beifengtz.jvmm.common.exception.ExecutionException;
import org.beifengtz.jvmm.common.util.CommonUtil;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.common.util.PlatformUtil;
import org.beifengtz.jvmm.common.util.SystemPropertyUtil;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.JvmmProfiler;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerCounter;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: async profiler实现
 * <p>
 * 在linux中如果内核权限访问被限制，执行命令： sudo sysctl -w kernel.perf_event_paranoid=1
 * </p>
 *
 * @author beifengtz
 * <a href="https://github.com/jvm-profiling-tools/async-profiler">https://github.com/jvm-profiling-tools/async-profiler</a>
 * <p>
 * Created in 6:28 下午 2021/6/23
 */
public class AsyncProfiler {

    private static AsyncProfiler instance = null;

    private static String libPath;

    static {
        String soPath = null;

        if (PlatformUtil.isMac()) {
            //  兼容 arm64 和 x86_64 架构
            soPath = "/async-profiler/libasyncProfiler-macos-x64-arm64.so";
        } else if (PlatformUtil.isLinux()) {
            if (PlatformUtil.isArm32()) {
                soPath = "/async-profiler/libasyncProfiler-linux-arm.so";
            } else if (PlatformUtil.isArm64()) {
                soPath = "/async-profiler/libasyncProfiler-linux-arm64.so";
            } else if (PlatformUtil.isX86()) {
                soPath = "/async-profiler/libasyncProfiler-linux-x86.so";
            } else if (PlatformUtil.isAarch64()) {
                soPath = "/async-profiler/libasyncProfiler-linux-aarch64.so";
            } else {
                soPath = "/async-profiler/libasyncProfiler-linux-x64.so";
            }
        }

        if (soPath != null) {
            InputStream stream = AsyncProfiler.class.getResourceAsStream(soPath);
            if (stream != null) {
                File file = new File(FileUtil.getTempPath(), "libasyncProfiler.so");
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if (file.exists()) {
                    file.delete();
                }
                try {
                    Files.copy(stream, file.toPath());
                    libPath = file.getAbsolutePath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private AsyncProfiler() {
    }

    public static synchronized AsyncProfiler getInstance() {
        try {
            return getInstance(null);
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }

    public static synchronized AsyncProfiler getInstance(String soFilePath) throws IOException {

        if (instance != null) {
            return instance;
        }

        if (soFilePath != null) {
            libPath = soFilePath;
        }

        if (libPath == null) {
            throw new IllegalStateException("Can not found libasyncProfiler.so, Only support Linux & Mac.");
        }

        //  不可重复加载
        if (!SystemPropertyUtil.getBoolean(SystemPropertyUtil.PROPERTY_JVMM_PROFILER_LOADED, false)) {
            System.load(libPath);
            System.setProperty(SystemPropertyUtil.PROPERTY_JVMM_PROFILER_LOADED, "true");
        }

        instance = new AsyncProfiler();
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

    public void addThread(Thread thread) {
        filterThread(thread, true);
    }

    public void removeThread(Thread thread) {
        filterThread(thread, false);
    }

    /**
     * 添加或移除指定线程，这些线程是被采样的对象
     *
     * @param thread 线程对象
     * @param enable true-添加 false-移除
     */
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

    /**
     * 命令格式：<a href="https://github.com/jvm-profiling-tools/async-profiler/blob/v1.8.1/src/arguments.cpp#L50">async-profiler</a>
     *
     * @param command 命令行参数错误
     * @return 响应结果
     * @throws IllegalArgumentException 命令行参数错误
     * @throws IOException              IO异常
     */
    private native String execute0(String command) throws IllegalArgumentException, IOException;

    private native void filterThread0(Thread thread, boolean enable);

    public static void main(String[] args) throws Exception {
        JvmmProfiler profiler = JvmmFactory.getProfiler();
        System.out.println(profiler.isSystemSupported());
        System.out.println(profiler.enabledEvents());
        System.out.println(profiler.version());
        System.out.println("status: " + profiler.status());
        System.out.println("start to dump file...");
        Future<?> future = profiler.sample(new File("test.html"), ProfilerEvent.wall.name(), 10, TimeUnit.SECONDS);
        System.out.println("status: " + profiler.status());
        System.out.println(future.get(12, TimeUnit.SECONDS));

        new Thread(() -> {
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                CommonUtil.print(random.nextInt(100));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        future = profiler.sample(new File("test-method.html"), "java.util.Random.nextInt", ProfilerCounter.total, 10, TimeUnit.SECONDS);
        System.out.println(future.get(12, TimeUnit.SECONDS));
    }
}
