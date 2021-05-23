package org.beifengtz.jvmm.core;


import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ExecutionError;
import org.beifengtz.jvmm.core.entity.result.JpsResult;
import org.beifengtz.jvmm.tools.util.IOUtil;
import org.beifengtz.jvmm.tools.util.JavaEnvUtil;
import org.beifengtz.jvmm.tools.util.PlatformUtil;
import org.beifengtz.jvmm.tools.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
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

    private static final Set<String> ENABLED_PROGRAM_NAME = ImmutableSet.of("jps");

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
    public void setThreadCpuTimeEnabled(boolean enabled) {
        ManagementFactory.getThreadMXBean().setThreadCpuTimeEnabled(enabled);
        log.info("Jvmm trigger execution of 'setThreadCpuTimeEnabled', param:{}", enabled);
    }

    @Override
    public void setThreadContentionMonitoringEnabled(boolean enabled) {
        ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(enabled);
        log.info("Jvmm trigger execution of 'setThreadContentionMonitoringEnabled', param:{}", enabled);
    }

    @Override
    public void resetPeakThreadCount() {
        ManagementFactory.getThreadMXBean().resetPeakThreadCount();
        log.info("Jvmm trigger execution of 'resetPeakThreadCount'");
    }

    @Override
    public InputStream executeJvmTools(String command, long timeout, TimeUnit timeUnit) throws IOException, TimeoutException {
        if (StringUtil.isEmpty(command)) {
            throw new IllegalArgumentException("Can not execute empty command.");
        }

        String programName = command.split("\\s")[0];
        if (!ENABLED_PROGRAM_NAME.contains(programName)) {
            throw new IllegalArgumentException("The command execution is not supported: " + programName);
        }

        String path = JavaEnvUtil.findJavaProgram(programName);
        if (path == null) {
            throw new IllegalArgumentException("Can not found java program: " + programName);
        }
        String newCmd = command.replaceFirst(programName, path);
        Process process = Runtime.getRuntime().exec(newCmd);

        try {
            if (process.waitFor(timeout, TimeUnit.SECONDS)) {
                if (process.exitValue() < 0) {
                    try (InputStream errIs = process.getErrorStream()) {
                        String error = IOUtil.toString(errIs, PlatformUtil.getEncoding());
                        throw new RejectedExecutionException(String.format("Execute command with exit value '%d'. %s. ['%s']"
                                , process.exitValue(), error, newCmd));
                    }
                }
            } else {
                throw new TimeoutException(String.format("Execute command time out. ['%s']", newCmd));
            }
        } catch (InterruptedException e) {
            log.error(String.format("Execute command error. ['%s']", newCmd), e);
        }
        return process.getInputStream();
    }

    @Override
    public List<JpsResult> listJavaProcess() {
        try (InputStream inputStream = executeJvmTools("jps -lmv", 50, TimeUnit.MILLISECONDS);
             Scanner sc = new Scanner(inputStream, PlatformUtil.getEncoding())) {
            List<JpsResult> resList = new LinkedList<>();
            while (sc.hasNext()) {
                String line = sc.nextLine();
                String[] s = line.split(" ");
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
            return resList;
        } catch (IOException | TimeoutException | RejectedExecutionException e) {
            log.error("List java process on localhost failed. " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }
}
