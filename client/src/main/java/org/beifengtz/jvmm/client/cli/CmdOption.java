package org.beifengtz.jvmm.client.cli;

/**
 * description: TODO
 * date: 15:00 2023/4/26
 *
 * @author beifengtz
 */
public class CmdOption implements Comparable<CmdOption>{
    private String name;
    private String argName;
    private String desc;
    private int order;

    private CmdOption() {
    }

    public static CmdOption create() {
        return new CmdOption();
    }

    public String getName() {
        return name;
    }

    public CmdOption setName(String name) {
        this.name = name;
        return this;
    }

    public String getArgName() {
        return argName;
    }

    public CmdOption setArgName(String argName) {
        this.argName = argName;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public CmdOption setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public int getOrder() {
        return order;
    }

    public CmdOption setOrder(int order) {
        this.order = order;
        return this;
    }

    @Override
    public int compareTo(CmdOption o) {
        int res = Integer.compare(order, o.order);
        if (res == 0) {
            res = name.compareTo(o.name);
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CmdOption cmdOption = (CmdOption) o;

        return name.equals(cmdOption.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
