package org.beifengtz.jvmm.core.entity.profiler;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 8:32 下午 2021/6/23
 *
 * @author beifengtz
 */
public interface EventType {
    String CPU = "cpu";
    String ALLOC = "alloc";
    String LOCK = "lock";
    String WALL = "wall";
    String ITIMER = "itimer";
}
