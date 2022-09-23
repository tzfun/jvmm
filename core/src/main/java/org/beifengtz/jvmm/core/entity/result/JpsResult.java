package org.beifengtz.jvmm.core.entity.result;

import org.beifengtz.jvmm.common.JsonParsable;

import java.util.List;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 17:08 2021/5/12
 *
 * @author beifengtz
 */
public class JpsResult implements JsonParsable {
    private long pid;
    private String mainClass;
    private List<String> arguments;

    private JpsResult(){
    }

    public static JpsResult create(){
        return new JpsResult();
    }

    public long getPid() {
        return pid;
    }

    public String getMainClass() {
        return mainClass;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public JpsResult setPid(long pid) {
        this.pid = pid;
        return this;
    }

    public JpsResult setMainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    public JpsResult setArguments(List<String> arguments) {
        this.arguments = arguments;
        return this;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
