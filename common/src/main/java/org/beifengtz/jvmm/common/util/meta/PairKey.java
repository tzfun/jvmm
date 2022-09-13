package org.beifengtz.jvmm.common.util.meta;

import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Description: TODO
 *
 * Created in 15:53 2021/10/27
 *
 * @author beifengtz
 */
public class PairKey<L, R> {
    private final L left;
    private final R right;

    private PairKey(final L left, final R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> PairKey<L, R> of(L left, R right) {
        return new PairKey<>(left, right);
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairKey<?, ?> pairKey = (PairKey<?, ?>) o;
        return Objects.equal(left, pairKey.left) && Objects.equal(right, pairKey.right);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(left).append(right).toHashCode();
    }
}
