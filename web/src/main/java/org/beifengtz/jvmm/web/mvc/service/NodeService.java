package org.beifengtz.jvmm.web.mvc.service;

import org.beifengtz.jvmm.common.exception.JvmmConnectFailedException;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.beifengtz.jvmm.web.entity.dto.NodeDTO;
import org.beifengtz.jvmm.web.entity.po.NodePO;
import org.beifengtz.jvmm.web.manage.factory.JvmmConnectorFactory;
import org.beifengtz.jvmm.web.mvc.dao.NodeConfMapper;
import org.beifengtz.jvmm.web.mvc.dao.NodeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Description: TODO
 * <p>
 * Created in 11:23 2022/2/25
 *
 * @author beifengtz
 */
@Service
public class NodeService {

    @Resource
    private NodeMapper nodeMapper;
    @Resource
    private NodeConfMapper nodeConfMapper;
    @Resource
    private JvmmConnectorFactory jvmmConnectorFactory;
    @Resource
    private CollectService collectService;

    @Transactional
    public int newNode(NodeDTO nodeInfo) {
        tryConnect(nodeInfo.getNode());
        nodeMapper.insert(nodeInfo.getNode());
        int nodeId = nodeInfo.getNode().getId();
        nodeInfo.getNodeConf().setId(nodeId);
        nodeConfMapper.insert(nodeInfo.getNodeConf());
        collectService.addScheduleTask(nodeId);
        return nodeId;
    }

    @Transactional
    public void updateNode(NodeDTO nodeInfo) {
        tryConnect(nodeInfo.getNode());
        nodeMapper.updateById(nodeInfo.getNode());
        nodeConfMapper.updateById(nodeInfo.getNodeConf());
        collectService.refreshScheduleTask(nodeInfo.getNode().getId());
    }

    @Transactional
    public void delNode(int nodeId) {
        nodeMapper.deleteById(nodeId);
        nodeConfMapper.deleteById(nodeId);
        collectService.remScheduleTask(nodeId);
    }

    public void tryConnect(NodePO node) throws JvmmConnectFailedException {
        try {
            JvmmConnector connector = jvmmConnectorFactory.getConnector(node);
            connector.ping();
        } catch (Exception e) {
            throw new JvmmConnectFailedException(e.getMessage(), e);
        }
    }
}
