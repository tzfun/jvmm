package org.beifengtz.jvmm.web.mvc.controller;

import org.beifengtz.jvmm.web.common.Constant;
import org.beifengtz.jvmm.web.entity.vo.ResultVO;
import org.beifengtz.jvmm.web.manage.factory.ResultFactory;
import org.beifengtz.jvmm.web.mvc.service.JvmmService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Description: TODO
 *
 * Created in 15:13 2022/1/11
 *
 * @author beifengtz
 */
@RestController
public class JvmmController {

    @Resource
    private JvmmService jvmmService;
    @Resource
    private ResultFactory resultFactory;

    @PostMapping(Constant.API_PUB + "/login")
    public ResultVO<String> login(@RequestParam String username, @RequestParam String password) {
        return resultFactory.success(jvmmService.login(username, password));
    }

    @GetMapping(Constant.API_NON_PUB + "/verify_token")
    public ResultVO<?> verifyToken() {
        return resultFactory.success();
    }
}
