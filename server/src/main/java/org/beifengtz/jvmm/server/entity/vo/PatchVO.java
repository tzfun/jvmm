package org.beifengtz.jvmm.server.entity.vo;

/**
 * Description: TODO
 *
 * Created in 16:33 2022/9/26
 *
 * @author beifengtz
 */
public class PatchVO {
    private String className;
    private int classLoaderHash;

    public String getClassName() {
        return className;
    }

    public PatchVO setClassName(String className) {
        this.className = className;
        return this;
    }

    public int getClassLoaderHash() {
        return classLoaderHash;
    }

    public PatchVO setClassLoaderHash(int classLoaderHash) {
        this.classLoaderHash = classLoaderHash;
        return this;
    }
}
