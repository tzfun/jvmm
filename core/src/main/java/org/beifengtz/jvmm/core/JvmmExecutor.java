package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.core.entity.result.JpsResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

    InputStream executeJvmTools(String command) throws IOException;

    List<JpsResult> listJavaProcess();
}
