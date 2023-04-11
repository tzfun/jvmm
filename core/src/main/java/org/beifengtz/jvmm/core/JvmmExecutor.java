package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.common.util.meta.PairKey;
import org.beifengtz.jvmm.core.entity.result.JpsResult;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
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

    void setThreadCpuTimeEnabled(boolean enable);

    void setThreadContentionMonitoringEnabled(boolean enable);

    void resetPeakThreadCount();

    PairKey<List<String>, Boolean> executeJvmTools(String command) throws IOException, TimeoutException, InterruptedException;

    PairKey<List<JpsResult>, String> listJavaProcess();

    File flameProfile(int pid, int sampleSeconds) throws IOException;

    File flameProfile(int pid, int sampleSeconds, String mode) throws IOException;

    void flameProfile(File to, int pid, int sampleSeconds) throws IOException;

    void flameProfile(File to, int pid, int sampleSeconds, String mode) throws IOException;

    /**
     * 代码反编译
     * @param instrumentation   Agent提供的探针
     * @param className         需要被反编译的类
     * @param methodName        需要被反编译类中的方法
     * @return  源码
     * @throws Exception 调用异常
     */
    String jad(Instrumentation instrumentation, String className, String methodName) throws Exception;
}
