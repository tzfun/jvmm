package org.beifengtz.jvmm.core;


import org.beifengtz.jvmm.common.util.CommonUtil;
import org.beifengtz.jvmm.common.util.ExecuteNativeUtil;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.common.util.JavaEnvUtil;
import org.beifengtz.jvmm.common.util.PlatformUtil;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.common.util.SystemPropertyUtil;
import org.beifengtz.jvmm.common.util.meta.PairKey;
import org.beifengtz.jvmm.core.entity.result.JpsResult;
import org.beifengtz.jvmm.core.ext.jad.JadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:18 2021/5/12
 *
 * @author beifengtz
 */
class DefaultJvmmExecutor implements JvmmExecutor {

    private static final Logger log = LoggerFactory.getLogger(DefaultJvmmExecutor.class);

    private static final Set<String> ENABLED_TOOL_SCRIPT = CommonUtil.hashSetOf("jps", "jstat", "jmap", "jcmd", "jstack", "jinfo");

    DefaultJvmmExecutor() {
    }


    @Override
    public void gc() {
        Runtime.getRuntime().gc();
        log.info("Jvmm trigger execution of 'gc'");
    }

    @Override
    public void setClassLoadingVerbose(boolean verbose) {
        ManagementFactory.getClassLoadingMXBean().setVerbose(verbose);
        log.info("Jvmm trigger execution of 'setClassLoadingVerbose', param:{}", verbose);
    }

    @Override
    public void setMemoryVerbose(boolean verbose) {
        ManagementFactory.getMemoryMXBean().setVerbose(verbose);
        log.info("Jvmm trigger execution of 'setMemoryVerbose', param:{}", verbose);
    }

    @Override
    public void setThreadCpuTimeEnabled(boolean enable) {
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        mx.setThreadCpuTimeEnabled(enable);

        boolean checkValue = mx.isThreadCpuTimeEnabled();
        if (enable != checkValue) {
            log.error("Could not set threadCpuTimeEnabled to " + enable + ", got " + checkValue + " instead");
        } else {
            log.info("Jvmm trigger execution of 'setThreadCpuTimeEnabled', param:{}", enable);
        }
    }

    @Override
    public void setThreadContentionMonitoringEnabled(boolean enable) {
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        mx.setThreadContentionMonitoringEnabled(enable);
        boolean checkValue = mx.isThreadContentionMonitoringEnabled();
        if (enable != checkValue) {
            log.error("Could not set threadContentionMonitoringEnabled to " + enable + ", got " + checkValue + " instead");
        } else {
            log.info("Jvmm trigger execution of 'setThreadContentionMonitoringEnabled', param:{}", enable);
        }
    }

    @Override
    public void resetPeakThreadCount() {
        ManagementFactory.getThreadMXBean().resetPeakThreadCount();
        log.info("Jvmm trigger execution of 'resetPeakThreadCount'");
    }

    @Override
    public PairKey<List<String>, Boolean> executeJvmTools(String command) throws IOException, TimeoutException, InterruptedException {
        if (StringUtil.isEmpty(command)) {
            throw new IllegalArgumentException("Can not execute empty command.");
        }

        String programName = command.split("\\s")[0];
        if (!ENABLED_TOOL_SCRIPT.contains(programName)) {
            throw new IllegalArgumentException("The command execution is not supported: " + programName);
        }

        String path = JavaEnvUtil.findJavaProgram(programName);
        if (path == null) {
            throw new IllegalArgumentException("Can not found java program: " + programName);
        }
        String newCmd = command.replaceFirst(programName, path);
        Process process = Runtime.getRuntime().exec(newCmd);

        List<String> output = new LinkedList<>();
        List<String> err = new LinkedList<>();

        try (Scanner sc = new Scanner(process.getInputStream(), PlatformUtil.getEncoding())) {
            while (sc.hasNext()) {
                output.add(sc.nextLine());
            }
        }

        try (Scanner sc = new Scanner(process.getErrorStream(), PlatformUtil.getEncoding())) {
            while (sc.hasNext()) {
                err.add(sc.nextLine());
            }
        }

        if (process.waitFor() != 0) {
            err.addAll(output);
            String errOutput = CommonUtil.join("\n", err);
            log.error("Execute command with exit value '{}'. {}. [{}]", process.exitValue(), errOutput, newCmd);
            return PairKey.of(err, false);
        } else {
            return PairKey.of(output, true);
        }
    }

    @Override
    public PairKey<List<JpsResult>, String> listJavaProcess() {
        List<JpsResult> resList = new LinkedList<>();
        String error = null;
        try {
            PairKey<List<String>, Boolean> PairKey = executeJvmTools("jps -lmv");

            if (PairKey.getRight()) {
                for (String line : PairKey.getLeft()) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] s = line.trim().split("\\s+");
                    JpsResult res = JpsResult.create();
                    res.setPid(Long.parseLong(s[0]));
                    if (s.length > 1) {
                        res.setMainClass(s[1]);
                    }
                    if (s.length > 2) {
                        List<String> args = new ArrayList<>(s.length - 2);
                        args.addAll(Arrays.asList(s).subList(2, s.length));
                        res.setArguments(args);
                    }
                    resList.add(res);
                }
            } else {
                error = CommonUtil.join("\n", PairKey.getLeft());
            }
        } catch (IOException | TimeoutException | InterruptedException e) {
            error = "List java process on localhost failed. " + e.getMessage();
            log.error(error, e);
        }
        return PairKey.of(resList, error);
    }

    @Override
    public File flameProfile(int pid, int sampleSeconds) throws IOException {
        return this.flameProfile(pid, sampleSeconds, "cpu");
    }

    @Override
    public File flameProfile(int pid, int sampleSeconds, String mode) throws IOException {
        File f = new File(SystemPropertyUtil.get("user.dir").replaceAll("\\\\", "/") + "/data",
                UUID.randomUUID() + ".svg");
        this.flameProfile(f, pid, sampleSeconds, mode);
        return f;
    }

    @Override
    public void flameProfile(File to, int pid, int sampleSeconds) throws IOException {
        this.flameProfile(to, pid, sampleSeconds, "cpu");
    }

    @Override
    public void flameProfile(File to, int pid, int sampleSeconds, String mode) throws IOException {
        URL resource = getClass().getResource("/profiler.sh");
        if (resource != null) {
            if (!to.getParentFile().exists()) {
                to.getParentFile().mkdirs();
            }
            List<String> result = ExecuteNativeUtil.execute(new String[]{resource.getFile(),
                    "-d", String.valueOf(sampleSeconds),
                    "-f", to.toString(),
                    "-e", mode,
                    String.valueOf(pid)});
            if (result.size() != 0) {
                throw new RejectedExecutionException(result.toString());
            }
        } else {
            throw new IOException("Script file not found: profiler.sh");
        }
    }

    @Override
    public String jad(Instrumentation instrumentation, String className, String methodName) throws Exception {
        if (instrumentation == null) {
            throw new IllegalStateException("No instrumentation");
        }
        byte[] bytes = JadUtil.toBytes(instrumentation, className);
        File file = new File(FileUtil.getTempPath(), className + ".class");
        FileUtil.writeByteArrayToFile(file, bytes);
        return JadUtil.decompile(file, methodName);
    }
}
