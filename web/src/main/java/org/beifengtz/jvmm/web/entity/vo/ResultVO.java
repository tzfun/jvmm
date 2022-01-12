package org.beifengtz.jvmm.web.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: TODO
 *
 * Created in 15:23 2022/1/12
 *
 * @author beifengtz
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultVO<T> {

    public Integer status;
    public String msg;
    public T data;
}
