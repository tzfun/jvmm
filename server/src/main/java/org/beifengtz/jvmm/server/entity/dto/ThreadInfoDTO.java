package org.beifengtz.jvmm.server.entity.dto;

import java.util.List;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 17:42 2022/9/21
 *
 * @author beifengtz
 */
public class ThreadInfoDTO {
    private long[] idArr;
    private int depth = 0;

    public long[] getIdArr() {
        return idArr;
    }

    public int getDepth() {
        return depth;
    }
}
