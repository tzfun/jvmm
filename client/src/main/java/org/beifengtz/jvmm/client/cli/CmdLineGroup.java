package org.beifengtz.jvmm.client.cli;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * description: TODO
 * date: 15:03 2023/4/26
 *
 * @author beifengtz
 */
public class CmdLineGroup {

    private String headDesc;
    private String tailDesc;
    /**
     * 窗口宽度，用于控制 Help 折行显示
     */
    private int windowWidth = 126;

    private final Set<CmdLine> commands = new TreeSet<>();

    private CmdLineGroup() {

    }

    public static CmdLineGroup create() {
        return new CmdLineGroup();
    }

    public String getHeadDesc() {
        return headDesc;
    }

    public CmdLineGroup setHeadDesc(String headDesc) {
        this.headDesc = headDesc;
        return this;
    }

    public String getTailDesc() {
        return tailDesc;
    }

    public CmdLineGroup setTailDesc(String tailDesc) {
        this.tailDesc = tailDesc;
        return this;
    }

    public CmdLine getCommand(String key) {
        CmdLine cmd = null;
        for (CmdLine command : commands) {
            if (Objects.equals(command.getKey(), key)) {
                cmd = command;
            }
        }
        return cmd;
    }

    public CmdLineGroup addCommand(CmdLine command) {
        commands.add(command);
        return this;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public CmdLineGroup setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
        return this;
    }

    public void printHelp() {
        if (headDesc != null) {
            System.out.println(CmdLine.breakLine(headDesc, getWindowWidth()));
        }
        int maxPrefix = 0;
        for (CmdLine command : commands) {
            maxPrefix = Math.max(maxPrefix, command.scanHelpMaxPrefix());
        }

        for (CmdLine command : commands) {
            command.printHelp(windowWidth, maxPrefix);
        }

        if (tailDesc != null) {
            System.out.println(CmdLine.breakLine(tailDesc, getWindowWidth()));
        }
    }

    public void printHelp(String key) {
        CmdLine cmd = getCommand(key);
        if (cmd == null) {
            System.out.println("Can not found command '" + key + "'");
        } else {
            cmd.printHelp(getWindowWidth(), -1);
        }
    }

    public static void main(String[] args) {
        CmdLineGroup group = CmdLineGroup.create()
                .addCommand(CmdLine.create().setKey("info").setArgPrefix("-")
                        .addOption(CmdOption.create().setName("t").setArgName("type").setOrder(1)
                                .setDesc("Sampling interval time, the unit is second. Default value: 10 s."))
                        .addOption(CmdOption.create().setName("i").setArgName("interval").setOrder(2)
                                .setDesc("The time interval of the unit to collect samples, the unit is nanosecond. Default value: 10000000 ns.")))
                .addCommand(CmdLine.create().setKey("collect").setArgPrefix("-")
                        .addOption(CmdOption.create().setName("f").setArgName("field").setOrder(1)
                                .setDesc("When querying info 'jvm_thread_pool'(required), this option is used to specify the thread pool. " +
                                        "If `ifield` is not filled, `field` will represent the static variable name of the thread " +
                                        "pool stored in `clazz`, and if `ifeld` is filled in, `field` will represent the property " +
                                        "variable name of the thread pool stored in the instance"))
                        .addOption(CmdOption.create().setName("if").setArgName("ifield").setOrder(2)
                                .setDesc("Required info type, optional values: \n- process\n- disk\n- disk_io\n- cpu" +
                                        "\n- network\n- sys\n- sys_memory\n- sys_file\n- jvm_classloading\n- jvm_classloader" +
                                        "\n- jvm_compilation\n- jvm_gc\n- jvm_memory\n- jvm_memory_manager\n- jvm_memory_pool" +
                                        "\n- jvm_thread\n- jvm_thread_stack\n- jvm_thread_detail\n- jvm_thread_pool")));
        group.printHelp();

        CmdParser parser = CmdParser.create(group.getCommand("info"), "info -t jvmm -b -i interval -m \"hello world\" -q");
        System.out.println(parser.getArg("t"));
        System.out.println(parser.getArg("i"));
        System.out.println(parser.hasArg("b"));
        System.out.println(parser.hasArg("q"));
        System.out.println(parser.hasArg("quq"));
        System.out.println(parser.getArg("m"));
    }
}
