package org.beifengtz.jvmm.common.tuple;

/**
 * Description: TODO
 *
 * Created in 17:42 2021/12/16
 *
 * @author beifengtz
 */
public class Pair<L, R> {
    private L left;
    private R right;

    private Pair() {
    }

    private Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s]", left, right);
    }
}
