package org.beifengtz.jvmm.client;

import org.beifengtz.jvmm.common.util.IPUtil;
import org.beifengtz.jvmm.common.util.PidUtil;
import org.beifengtz.jvmm.common.util.meta.PairKey;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.entity.result.JpsResult;

import java.io.Console;
import java.io.File;
import java.util.List;
import java.util.Objects;
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
        System.out.println("\n[1] client,\tConnect to remote jvmm server.");
        System.out.println("[2] attach,\tAttach Jvmm to the local java process.");
        System.out.println("[3] jar,\tGenerate the jar files required by the java agent.");
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
            return f.getAbsolutePath();
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
        List<JpsResult> jpsList = printJps();
        if (jpsList != null) {
            JpsResult jps = null;
            while (scanner.hasNextLine()) {
                String str = scanner.nextLine();
                int result = str.matches("\\d+") ? Integer.parseInt(str) : -1;

                if (result == 0) {
                    jpsList = printJps();
                } else if (result < 0 || result > jpsList.size()) {
                    System.out.println("Wrong serial number.");
                    System.out.print("Select the program number you will attach: ");
                } else {
                    jps = jpsList.get(result - 1);
                    break;
                }
            }
            assert jps != null;
            return (int) jps.getPid();
        }
        return -1;
    }

    private static List<JpsResult> printJps() {
        System.out.println();
        PairKey<List<JpsResult>, String> pairKey = JvmmFactory.getExecutor().listJavaProcess();
        if (pairKey.getRight() == null) {
            List<JpsResult> jpsList = pairKey.getLeft();

            jpsList.removeIf(o -> o.getPid() == PidUtil.currentPid() || o.getMainClass().endsWith("jps.Jps"));

            for (int i = 1; i <= jpsList.size(); i++) {
                JpsResult jps = jpsList.get(i - 1);
                System.out.printf("[%d]\t%d\t%s%n", i, jps.getPid(), jps.getMainClass());
            }

            System.out.print("\nType 0 to reload processes list.\n");
            System.out.print("\nSelect the program number you will attach: ");

            return jpsList;
        } else {
            System.out.println("Can not get local java processes, case: " + pairKey.getRight());
            System.exit(1);
        }
        return null;
    }

    public static String askServerAddress() {
        System.out.print("Enter the Jvmm server address (default is 127.0.0.1:5010): ");
        String address = scanner.nextLine();
        if (address.isEmpty()) {
            address = "127.0.0.1:5010";
        }
        if (IPUtil.isHost(address)) {
            return address;
        }
        System.out.println("Wrong address format");
        return askServerAddress();
    }

    public static String askServerAuthUsername() {
        System.out.print("Please enter username:");
        return scanner.nextLine();
    }

    public static String askServerAuthPassword() {
        System.out.print("Please enter password:");
        Console console = System.console();
        if (console == null) {
            return scanner.nextLine();
        } else {
            return String.copyValueOf(console.readPassword());
        }
    }

    public static boolean askImportSlf4j() {
        System.out.print("Do you want to introduce slf4j dependency to jvmm-server.jar? (y/n, default yes): ");
        String result = scanner.nextLine();
        return result.isEmpty() || Objects.equals(result, "y");
    }
}
