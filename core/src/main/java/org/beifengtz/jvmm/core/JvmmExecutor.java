package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.common.tuple.Pair;
import org.beifengtz.jvmm.core.entity.result.JpsResult;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
public interface JvmmExecutor {

    void gc();

    void setClassLoadingVerbose(boolean verbose);

    void setMemoryVerbose(boolean verbose);

    void setThreadCpuTimeEnabled(boolean enabled);

    void setThreadContentionMonitoringEnabled(boolean enabled);

    void resetPeakThreadCount();

    Pair<List<String>, Boolean> executeJvmTools(String command) throws IOException, TimeoutException, InterruptedException;

    Pair<List<JpsResult>, String> listJavaProcess();

    File flameProfile(int pid, int sampleSeconds) throws IOException;

    File flameProfile(int pid, int sampleSeconds, String mode) throws IOException;

    void flameProfile(File to, int pid, int sampleSeconds) throws IOException;

    void flameProfile(File to, int pid, int sampleSeconds, String mode) throws IOException;
}
