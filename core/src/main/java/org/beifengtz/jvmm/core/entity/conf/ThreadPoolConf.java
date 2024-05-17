package org.beifengtz.jvmm.core.entity.conf;

/**
 * description TODO
 * date 16:27 2023/6/22
 *
 * @author beifengtz
 */
public class ThreadPoolConf {
    private String name;
    private String classPath;
    private String instanceFiled;
    private String filed;

    public String getName() {
        return name;
    }

    public ThreadPoolConf setName(String name) {
        this.name = name;
        return this;
    }

    public String getClassPath() {
        return classPath;
    }

    public ThreadPoolConf setClassPath(String classPath) {
        this.classPath = classPath;
        return this;
    }

    public String getInstanceFiled() {
        return instanceFiled;
    }

    public ThreadPoolConf setInstanceFiled(String instanceFiled) {
        this.instanceFiled = instanceFiled;
        return this;
    }

    public String getFiled() {
        return filed;
    }

    public ThreadPoolConf setFiled(String filed) {
        this.filed = filed;
        return this;
    }
}
