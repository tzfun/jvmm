package org.beifengtz.jvmm.web.manage.factory;

import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.web.common.RestfulStatus;
import org.beifengtz.jvmm.web.entity.vo.ResultVO;
import org.springframework.stereotype.Component;

/**
 * Description: TODO
 *
 * Created in 15:29 2022/1/12
 *
 * @author beifengtz
 */
@Component
public class ResultFactory {
    public <T> ResultVO<T> success(T data) {
        return new ResultVO<T>(RestfulStatus.OK.getStatus(), RestfulStatus.OK.getMsg(), data);
    }

    public ResultVO<?> success() {
        return success(null);
    }

    private <T> ResultVO<T> error(Integer status, String msg, T data) {
        return new ResultVO<T>(status, msg, data);
    }

    public ResultVO<?> error(RestfulStatus statusEnum, String msg) {
        return error(statusEnum.getStatus(), StringUtil.emptyOrDefault(msg, statusEnum.getMsg()), null);
    }

    public <T> ResultVO<T> error(RestfulStatus statusEnum, T data) {
        return error(statusEnum.getStatus(), statusEnum.getMsg(), data);
    }

    public <T> ResultVO<T> error(RestfulStatus statusEnum, String msg, T data) {
        return error(statusEnum.getStatus(), msg, data);
    }

    public ResultVO<?> error(RestfulStatus statusEnum) {
        return error(statusEnum, statusEnum.getMsg());
    }

    public ResultVO<?> error(String msg) {
        return error(RestfulStatus.SERVER_ERR.getStatus(), msg, null);
    }

    public ResultVO<?> error() {
        return error(RestfulStatus.SERVER_ERR);
    }
}
