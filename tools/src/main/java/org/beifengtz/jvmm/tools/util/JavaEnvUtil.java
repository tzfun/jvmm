package org.beifengtz.jvmm.tools.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:17 2021/5/12
 *
 * @author beifengtz
 */
public class JavaEnvUtil {

    private static final Logger log = LoggerFactory.getLogger(JavaEnvUtil.class);

    private static volatile String JAVA_HOME = null;

    public static String findJavaHome() {
        if (JAVA_HOME != null) {
            return JAVA_HOME;
        }

        String javaHome = System.getProperty("java.home");

        if (JavaVersionUtils.isLessThanJava9()) {
            File toolsJar = new File(javaHome, "lib/tools.jar");
            if (!toolsJar.exists()) {
                toolsJar = new File(javaHome, "../lib/tools.jar");
            }
            if (!toolsJar.exists()) {
                // maybe jre
                toolsJar = new File(javaHome, "../../lib/tools.jar");
            }

            if (toolsJar.exists()) {
                JAVA_HOME = javaHome;
                return JAVA_HOME;
            }

            if (!toolsJar.exists()) {
                log.debug("Can not find tools.jar under java.home: {}", javaHome);
                String javaHomeEnv = System.getenv("JAVA_HOME");
                if (javaHomeEnv != null && !javaHomeEnv.isEmpty()) {
                    log.debug("Try to find tools.jar in System Env JAVA_HOME: {}", javaHomeEnv);
                    // $JAVA_HOME/lib/tools.jar
                    toolsJar = new File(javaHomeEnv, "lib/tools.jar");
                    if (!toolsJar.exists()) {
                        // maybe jre
                        toolsJar = new File(javaHomeEnv, "../lib/tools.jar");
                    }
                }

                if (toolsJar.exists()) {
                    log.info("Found java home from System Env JAVA_HOME: {}", javaHomeEnv);
                    JAVA_HOME = javaHomeEnv;
                    return JAVA_HOME;
                }

                throw new IllegalArgumentException("Can not find tools.jar under java home: " + javaHome
                        + ", please try to start arthas-boot with full path java. Such as /opt/jdk/bin/java -jar arthas-boot.jar");
            }
        } else {
            JAVA_HOME = javaHome;
        }
        return JAVA_HOME;
    }

    public static File findToolsJar(String javaHome) {
//        if (JavaVersionUtils.isGreaterThanJava8()) {
//            return null;
//        }

        File toolsJar = new File(javaHome, "lib/tools.jar");
        if (!toolsJar.exists()) {
            toolsJar = new File(javaHome, "../lib/tools.jar");
        }
        if (!toolsJar.exists()) {
            // maybe jre
            toolsJar = new File(javaHome, "../../lib/tools.jar");
        }

        if (!toolsJar.exists()) {
            throw new IllegalArgumentException("Can not find tools.jar under java home: " + javaHome);
        }

        log.debug("Found tools.jar: {}", toolsJar.getAbsolutePath());
        return toolsJar;
    }

    /**
     * 搜索java程序全路径
     *
     * @param programName 程序名，不需要文件后缀
     * @return 程序所在全路径
     */
    public static String findJavaProgram(String programName) {
        String javaHome = SystemPropertyUtil.get("java.home");
        String[] paths = {"bin/" + programName, "bin/" + programName + ".exe", "../bin/" + programName, "../bin/" + programName + ".exe"};

        List<File> programList = new ArrayList<>();
        for (String path : paths) {
            File programFile = new File(javaHome, path);
            if (programFile.exists()) {
                log.debug("Found '{}': {}", programName, programFile.getAbsolutePath());
                programList.add(programFile);
            }
        }

        if (programList.isEmpty()) {
            log.debug("Can not find '{}' under :{}", programName, javaHome);
            String javaHomeEnv = System.getenv("JAVA_HOME");
            log.debug("Try to find '{}' under env JAVA_HOME :{}", programName, javaHomeEnv);
            for (String path : paths) {
                File programFile = new File(javaHomeEnv, path);
                if (programFile.exists()) {
                    log.debug("Found '{}': {}", programName, programFile.getAbsolutePath());
                    programList.add(programFile);
                }
            }
        }

        if (programList.isEmpty()) {
            log.debug("Can not find '{}' under current java home: {}", programName, javaHome);
            return null;
        }

        // find the shortest path, jre path longer than jdk path
        if (programList.size() > 1) {
            programList.sort((file1, file2) -> {
                try {
                    return file1.getCanonicalPath().length() - file2.getCanonicalPath().length();
                } catch (IOException e) {
                    // ignore
                }
                return -1;
            });
        }
        return programList.get(0).getAbsolutePath().replaceAll("\\\\", "/");
    }
}
