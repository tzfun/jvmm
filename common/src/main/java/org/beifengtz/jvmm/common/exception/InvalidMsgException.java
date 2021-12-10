package org.beifengtz.jvmm.common.exception;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 8:42 下午 2021/5/29
 *
 * @author beifengtz
 */
public class InvalidMsgException extends RuntimeException {

    private int seed;

    public InvalidMsgException() {
    }

    public InvalidMsgException(int seed) {
        this.seed = seed;
    }

    public int getSeed() {
        return seed;
    }
}
