package org.beifengtz.jvmm.client.cli;

import org.beifengtz.jvmm.common.util.StringUtil;

import java.util.Set;
import java.util.TreeSet;

/**
 * description: TODO
 * date: 14:59 2023/4/26
 *
 * @author beifengtz
 */
public class CmdLine implements Comparable<CmdLine> {
    private String key;
    private String argPrefix = "-";
    private int order;
    private final Set<CmdOption> options = new TreeSet<>();
    private String headDesc;
    private String tailDesc;

    private CmdLine() {
    }

    public static CmdLine create() {
        return new CmdLine();
    }

    /**
     * 按照 width 折行，其中需要保留单词完整性
     *
     * @param line  行字符串
     * @param width 超过此宽度进行折行
     * @return 这行后的字符串
     */
    public static String breakLine(String line, int width) {
        if (line == null) return null;
        if (line.length() > width) {
            StringBuilder sb = new StringBuilder();
            int idx = 0, headIdx = 0, count = 0;
            while (idx < line.length()) {
                char ch = line.charAt(idx);
                if (ch == '\n') {
                    count = 0;
                } else {
                    count++;
                }
                if (count == width) {
                    while (idx >= 0) {
                        char c = line.charAt(idx);
                        if (c == '-' || c == ' ' || c == ',' || c == '.' || c == '!' || c == '?') {
                            break;
                        }
                        idx--;
                    }
                    sb.append(line, headIdx, idx + 1).append("\n");
                    headIdx = ++idx;
                    count = 0;
                } else {
                    idx++;
                }
            }
            sb.append(line, headIdx, line.length());
            return sb.toString();
        } else {
            return line;
        }
    }

    int scanHelpMaxPrefix() {
        int maxPreCharNum = 0;

        for (CmdOption option : getOptions()) {
            int preCharNum = 0;
            preCharNum += getArgPrefix().length();
            preCharNum += option.getName().length();
            if (option.getArgName() != null) {
                preCharNum += 3 + option.getArgName().length();
            }
            maxPreCharNum = Math.max(maxPreCharNum, preCharNum);
        }

        int tableFillChar = maxPreCharNum % 4;
        if (tableFillChar == 0) {
            maxPreCharNum += 4;
        } else {
            maxPreCharNum += tableFillChar;
        }
        maxPreCharNum += 4;
        return maxPreCharNum;
    }

    /**
     * 为了达到最优的视觉体验，一行显示内容将限制在 consoleWidth 范围内，超出会根据单词折行
     *
     * @param consoleWidth 终端宽度
     */
    public void printHelp(int consoleWidth, int maxPrefix) {
        if (headDesc != null) {
            if (StringUtil.isEmpty(key)) {
                System.out.println(breakLine(headDesc, consoleWidth));
            } else {
                System.out.println(key + ":\n" + breakLine(headDesc, consoleWidth));
            }
        } else {
            if (StringUtil.isEmpty(key)) {
                System.out.println();
            } else {
                System.out.println(key + ":");
            }
        }

        int maxPreCharNum = maxPrefix > 0 ? maxPrefix : scanHelpMaxPrefix();

        for (CmdOption option : getOptions()) {
            int preCharNum = 4;
            String line = StringUtil.repeat(" ", 4) + getArgPrefix();
            preCharNum += getArgPrefix().length();

            line += option.getName();
            preCharNum += option.getName().length();
            if (option.getArgName() != null) {
                line += " <" + option.getArgName() + ">";
                preCharNum += 3 + option.getArgName().length();
            }

            line += StringUtil.repeat(" ", maxPreCharNum - preCharNum);
            String desc = option.getDesc();
            if (desc != null) {
                desc = breakLine(desc, consoleWidth - maxPreCharNum);
                line += desc.replaceAll("\n", "\n" + StringUtil.repeat(" ", maxPreCharNum));
            }
            System.out.println(line);

        }

        if (tailDesc != null) {
            System.out.println(breakLine(tailDesc, consoleWidth));
        }
        System.out.println();
    }

    public CmdLine setKey(String key) {
        this.key = key;
        return this;
    }

    public CmdLine setArgPrefix(String argPrefix) {
        this.argPrefix = argPrefix;
        return this;
    }

    public CmdLine addOption(CmdOption option) {
        options.add(option);
        return this;
    }

    Set<CmdOption> getOptions() {
        return options;
    }

    public String getKey() {
        return key;
    }

    String getArgPrefix() {
        return argPrefix;
    }

    public int getOrder() {
        return order;
    }

    public CmdLine setOrder(int order) {
        this.order = order;
        return this;
    }

    public String getHeadDesc() {
        return headDesc;
    }

    public CmdLine setHeadDesc(String headDesc) {
        if (StringUtil.nonEmpty(headDesc)) {
            this.headDesc = headDesc;
        }
        return this;
    }

    public String getTailDesc() {
        return tailDesc;
    }

    public CmdLine setTailDesc(String tailDesc) {
        if (StringUtil.nonEmpty(tailDesc)) {
            this.tailDesc = tailDesc;
        }
        return this;
    }

    @Override
    public int compareTo(CmdLine o) {
        int res = Integer.compare(order, o.order);
        if (res == 0) {
            res = key.compareTo(o.key);
        }
        return res;
    }
}
