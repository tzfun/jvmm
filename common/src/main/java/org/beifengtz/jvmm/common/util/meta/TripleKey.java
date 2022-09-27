package org.beifengtz.jvmm.common.util.meta;

import java.util.Objects;

/**
 * Description: TODO
 *
 * Created in 15:59 2021/10/27
 *
 * @author beifengtz
 */
public class TripleKey<L, M, R> {
    private final L left;
    private final M middle;
    private final R right;

    private TripleKey(L left, M middle, R right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    public static <L, M, R> TripleKey<L, M, R> of(L left, M middle, R right) {
        return new TripleKey<>(left, middle, right);
    }

    public L getLeft() {
        return left;
    }

    public M getMiddle() {
        return middle;
    }

    public R getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TripleKey<?, ?, ?> tripleKey = (TripleKey<?, ?, ?>) o;

        if (!Objects.equals(left, tripleKey.left)) return false;
        if (!Objects.equals(middle, tripleKey.middle)) return false;
        return Objects.equals(right, tripleKey.right);
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (middle != null ? middle.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s]", left, middle, right);
    }
}
