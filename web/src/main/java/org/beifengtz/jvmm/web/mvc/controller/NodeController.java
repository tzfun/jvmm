package org.beifengtz.jvmm.web.mvc.controller;

import org.beifengtz.jvmm.web.common.Constant;
import org.beifengtz.jvmm.web.entity.dto.NodeDTO;
import org.beifengtz.jvmm.web.entity.vo.ResultVO;
import org.beifengtz.jvmm.web.manage.factory.ResultFactory;
import org.beifengtz.jvmm.web.mvc.service.NodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Description: TODO
 *
 * Created in 11:24 2022/2/25
 *
 * @author beifengtz
 */
@RestController
public class NodeController {

    @Resource
    private NodeService nodeService;
    @Resource
    private ResultFactory resultFactory;

    @PostMapping(Constant.API_NON_PUB + "/node/new")
    public ResultVO<Integer> newNode(@RequestBody NodeDTO nodeInfo) {
        nodeInfo.check();
        nodeInfo.getNode().setCreateTime(System.currentTimeMillis());
        nodeInfo.getNode().setUpdateTime(System.currentTimeMillis());
        return resultFactory.success(nodeService.newNode(nodeInfo));
    }

    @PostMapping(Constant.API_NON_PUB + "/node/update")
    public ResultVO<?> updateNode(@RequestBody NodeDTO nodeInfo) {
        nodeInfo.check();
        nodeInfo.getNode().setUpdateTime(System.currentTimeMillis());
        nodeService.updateNode(nodeInfo);
        return resultFactory.success();
    }

    @GetMapping(Constant.API_NON_PUB + "/node/del")
    public ResultVO<?> delNode(@RequestParam int nodeId) {
        nodeService.delNode(nodeId);
        return resultFactory.success();
    }
}
