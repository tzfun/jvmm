package org.beifengtz.jvmm.web.mvc.service;

import org.beifengtz.jvmm.common.exception.AuthenticationFailedException;
import org.beifengtz.jvmm.common.exception.JvmmConnectFailedException;
import org.beifengtz.jvmm.common.exception.SocketExecuteException;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.beifengtz.jvmm.web.entity.dto.NodeDTO;
import org.beifengtz.jvmm.web.entity.po.NodePO;
import org.beifengtz.jvmm.web.manage.factory.JvmmConnectorFactory;
import org.beifengtz.jvmm.web.mvc.dao.NodeConfMapper;
import org.beifengtz.jvmm.web.mvc.dao.NodeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeoutException;

/**
 * Description: TODO
 *
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

    @Transactional
    public int newNode(NodeDTO nodeInfo) {
        testConnect(nodeInfo.getNode());
        nodeMapper.insert(nodeInfo.getNode());
        int nodeId = nodeInfo.getNode().getId();
        nodeInfo.getNodeConf().setId(nodeId);
        nodeConfMapper.insert(nodeInfo.getNodeConf());
        return nodeId;
    }

    @Transactional
    public void updateNode(NodeDTO nodeInfo) {
        testConnect(nodeInfo.getNode());
        nodeMapper.updateById(nodeInfo.getNode());
        nodeConfMapper.updateById(nodeInfo.getNodeConf());
    }

    @Transactional
    public void delNode(int nodeId) {
        nodeMapper.deleteById(nodeId);
        nodeConfMapper.deleteById(nodeId);
    }

    public void testConnect(NodePO node) throws JvmmConnectFailedException {
        try {
            JvmmConnector connector = jvmmConnectorFactory.getConnector(node);
            connector.ping();
        } catch (TimeoutException | SocketExecuteException | AuthenticationFailedException e) {
            throw new JvmmConnectFailedException(e.getMessage(), e);
        }
    }
}
