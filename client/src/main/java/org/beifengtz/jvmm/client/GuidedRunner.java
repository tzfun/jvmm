package org.beifengtz.jvmm.client;

import org.beifengtz.jvmm.common.tuple.Pair;
import org.beifengtz.jvmm.common.util.PidUtil;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.entity.result.JpsResult;

import java.io.File;
import java.util.List;
import java.util.Scanner;

/**
 * <p>
 * Description: 引导式命令执行器，会根据缺少的命令提示引导填充相关参数，由CommandRunner调起
 * </p>
 * <p>
 * Created in 15:43 2022/9/19
 *
 * @author beifengtz
 */
public class GuidedRunner {

    private static final Scanner scanner = new Scanner(System.in);

    public static String askMode() {
        System.out.println("\n[1] client, Connect to remote jvmm server.");
        System.out.println("[2] attach, Attach Jvmm to the local java process.");
        System.out.println("[3] jar, Generate the jar files required by the java agent.");
        System.out.print("\nSelect an execution mode(serial number): ");

        String mode = null;
        while (scanner.hasNextLine()) {
            String str = scanner.nextLine();
            int result = str.matches("\\d+") ? Integer.parseInt(str) : 0;
            if (result == 1) {
                mode = "client";
                break;
            } else if (result == 2) {
                mode = "attach";
                break;
            } else if (result == 3) {
                mode = "jar";
                break;
            } else {
                System.out.println("\nWrong serial number.");
                System.out.print("Select an execution mode(serial number): ");
            }
        }
        return mode;
    }

    public static String askAgentFilePath() {
        File f = new File("jvmm-agent.jar");
        if (f.exists()) {
            System.out.print("Jar file jvmm-agent.jar was found in the current directory, do you want to use it?(Y/N) ");
            String res = scanner.nextLine();
            if ("y".equalsIgnoreCase(res)) {
                return f.getAbsolutePath();
            }
        }

        String path = null;
        System.out.print("Enter the jvmm-agent.jar file path, local file path and http(s) are supported: ");
        path = scanner.nextLine();
        return path;
    }

    public static String askServerFilePath() {
        File f = new File("jvmm-server.jar");
        if (f.exists()) {
            System.out.print("Jar file jvmm-server.jar was found in the current directory, do you want to use it?(Y/N) ");
            String res = scanner.nextLine();
            if ("y".equalsIgnoreCase(res)) {
                return f.getAbsolutePath();
            }
        }

        String path = null;
        System.out.print("Enter the jvmm-server.jar file path, local file path and http(s) are supported: ");
        path = scanner.nextLine();
        return path;
    }

    public static String askConfigFilePath() {

        String path = null;
        File f = new File("config.yml");
        if (f.exists()) {
            System.out.print("A configuration file config.yml was found in the current directory, do you want to use it?(Y/N) ");
            String res = scanner.nextLine();
            if ("y".equalsIgnoreCase(res)) {
                return f.getAbsolutePath();
            }
        }

        System.out.print("Enter the yaml config file path, local file path and http(s) are supported: ");
        while (scanner.hasNextLine()) {
            path = scanner.nextLine();
            if (path.startsWith("http://") || path.startsWith("https://")) {
                break;
            } else {
                File file = new File(path);
                if (file.exists()) {
                    path = file.getAbsolutePath();
                    System.out.println("Chosen config file absolute path is " + path);
                    break;
                } else {
                    System.out.println("File does not exists");
                    System.out.print("Enter the yaml config file path, local file path and http(s) are supported: ");
                }
            }
        }
        assert path != null;
        return path;
    }

    public static int askAttachPid() {
        System.out.println();
        Pair<List<JpsResult>, String> pair = JvmmFactory.getExecutor().listJavaProcess();
        if (pair.getRight() == null) {
            List<JpsResult> jpsList = pair.getLeft();

            long currentPid = PidUtil.currentPid();
            jpsList.removeIf(o -> o.getPid() == currentPid);

            for (int i = 1; i <= jpsList.size(); i++) {
                JpsResult jps = jpsList.get(i - 1);
                System.out.printf("[%d] %d %s%n", i, jps.getPid(), jps.getMainClass());
            }

            System.out.print("\nSelect the program number you will attach: ");
            JpsResult jps = null;
            while (scanner.hasNextLine()) {
                String str = scanner.nextLine();
                int result = str.matches("\\d+") ? Integer.parseInt(str) : 0;

                if (result <= 0 || result > jpsList.size()) {
                    System.out.println("Wrong serial number.");
                    System.out.print("Select the program number you will attach: ");
                } else {
                    jps = jpsList.get(result - 1);
                    break;
                }
            }
            assert jps != null;
            return (int) jps.getPid();
        } else {
            System.out.println("Can not get local java processes, case: " + pair.getRight());
            System.exit(-1);
        }
        return -1;
    }

    public static String askServerAddress() {
        System.out.print("Enter the Jvmm server address: ");
        return scanner.nextLine();
    }

    public static boolean askServerAuthEnable() {
        System.out.print("Does the jvmm server require authentication?(Y/N): ");
        String result = scanner.nextLine();
        return "y".equalsIgnoreCase(result);
    }

    public static String askServerAuthUsername() {
        System.out.print("Please enter username:");
        return scanner.nextLine();
    }

    public static String askServerAuthPassword() {
        System.out.print("Please enter password:");
        return scanner.nextLine();
    }
}
