package org.beifengtz.jvmm.server.entity.dto;

/**
 * Description: TODO
 *
 * Created in 16:10 2022/9/26
 *
 * @author beifengtz
 */
public class PatchDTO {
    private String className;
    private String hex;
    private Integer classLoaderHash;

    public String getClassName() {
        return className;
    }

    public PatchDTO setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getHex() {
        return hex;
    }

    public PatchDTO setHex(String hex) {
        this.hex = hex;
        return this;
    }

    public Integer getClassLoaderHash() {
        return classLoaderHash;
    }

    public PatchDTO setClassLoaderHash(Integer classLoaderHash) {
        this.classLoaderHash = classLoaderHash;
        return this;
    }
}
