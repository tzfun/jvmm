package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * description: JVM类加载器信息
 * date 17:59 2022/9/23
 *
 * @author beifengtz
 */
public class JvmClassLoaderInfo implements JsonParsable {
    private String name;
    private int hash;
    private final List<String> parents = new ArrayList<>();

    public static JvmClassLoaderInfo create(int hash) {
        return new JvmClassLoaderInfo().setHash(hash);
    }

    public String getName() {
        return name;
    }

    public JvmClassLoaderInfo setName(String name) {
        this.name = name;
        return this;
    }

    public int getHash() {
        return hash;
    }

    private JvmClassLoaderInfo setHash(int hash) {
        this.hash = hash;
        return this;
    }

    public List<String> getParents() {
        return parents;
    }

    public JvmClassLoaderInfo addParents(String parent) {
        this.parents.add(parent);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JvmClassLoaderInfo that = (JvmClassLoaderInfo) o;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
