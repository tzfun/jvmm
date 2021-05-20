package org.beifengtz.jvmm.agent;


import org.beifengtz.jvmm.tools.util.SystemPropertyUtil;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:45 2021/5/22
 *
 * @author beifengtz
 */
public class AppUtil {

    private static String HOME_PATH;
    private static String LOG_PATH;
    private static String DATA_PATH;

    static {
        try {
            HOME_PATH = SystemPropertyUtil.get("user.dir").replaceAll("\\\\", "/");

            LOG_PATH = HOME_PATH + "/logs";
            System.setProperty("jvmm.log.path", LOG_PATH);

            DATA_PATH = HOME_PATH + "/data";
            System.setProperty("jvmm.data.path", DATA_PATH);
        } catch (Exception e) {
            System.err.println("Init application failed. " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getLogPath() {
        return LOG_PATH;
    }

    public static String getDataPath() {
        return DATA_PATH;
    }

    public static String getHomePath() {
        return HOME_PATH;
    }

    public static String getTempPath() {
        return DATA_PATH + "/temp/";
    }
}
