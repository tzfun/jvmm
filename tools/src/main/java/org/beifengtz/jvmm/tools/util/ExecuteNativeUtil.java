package org.beifengtz.jvmm.tools.util;

import org.beifengtz.jvmm.tools.factory.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * <p>
 * Description: TODO
 * </p>
 *
 * <p>
 * Created in 15:46 2021/5/18
 *
 * @author beifengtz
 */
public class ExecuteNativeUtil {

    private static final Logger log = LoggerFactory.logger(ExecuteNativeUtil.class);

    public static List<String> execute(String command) {
        return execute(command.split(" "));
    }

    public static List<String> execute(String[] command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            LinkedList<String> result = new LinkedList<>();
            try (Scanner sc = new Scanner(process.getInputStream(), PlatformUtil.getEncoding())) {
                while (sc.hasNext()) {
                    result.add(sc.nextLine());
                }
            }
            process.waitFor();
            return result;
        } catch (Exception e) {
            log.error("Execute command with some problem: " + Arrays.toString(command), e);
            return new ArrayList<>();
        }
    }

    public static String executeForFirstLine(String command) {
        List<String> result = execute(command);
        if (result.size() > 0) {
            return result.get(0);
        }
        return "";
    }
}
