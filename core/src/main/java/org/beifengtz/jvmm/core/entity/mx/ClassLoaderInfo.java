package org.beifengtz.jvmm.core.entity.mx;

import org.beifengtz.jvmm.common.JsonParsable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Description: TODO
 *
 * Created in 17:59 2022/9/23
 *
 * @author beifengtz
 */
public class ClassLoaderInfo implements JsonParsable {
    private String name;
    private int hash;
    private List<String> parents = new ArrayList<>();

    public static ClassLoaderInfo create(int hash) {
        return new ClassLoaderInfo().setHash(hash);
    }

    public String getName() {
        return name;
    }

    public ClassLoaderInfo setName(String name) {
        this.name = name;
        return this;
    }

    public int getHash() {
        return hash;
    }

    private ClassLoaderInfo setHash(int hash) {
        this.hash = hash;
        return this;
    }

    public List<String> getParents() {
        return parents;
    }

    public ClassLoaderInfo addParents(String parent) {
        this.parents.add(parent);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassLoaderInfo that = (ClassLoaderInfo) o;
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
