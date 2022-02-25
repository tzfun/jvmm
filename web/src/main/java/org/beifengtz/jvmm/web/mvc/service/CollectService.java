package org.beifengtz.jvmm.web.mvc.service;

import com.baomidou.mybatisplus.mapper.Condition;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.beifengtz.jvmm.convey.GlobalStatus;
import org.beifengtz.jvmm.convey.GlobalType;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.beifengtz.jvmm.web.entity.po.NodeConfPO;
import org.beifengtz.jvmm.web.entity.po.NodePO;
import org.beifengtz.jvmm.web.manage.factory.JvmmConnectorFactory;
import org.beifengtz.jvmm.web.mvc.dao.NodeConfMapper;
import org.beifengtz.jvmm.web.mvc.dao.NodeMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Description: TODO
 *
 * Created in 18:09 2022/2/25
 *
 * @author beifengtz
 */
@Service
@Slf4j
public class CollectService {

    private static volatile boolean SCHEDULE_TASK_FLAG = true;
    private static final HashMap<Integer, ScheduleTask> TASK_MAP = new HashMap<>();

    @Resource
    private JvmmConnectorFactory jvmmConnectorFactory;
    @Resource
    private NodeMapper nodeMapper;
    @Resource
    private NodeConfMapper nodeConfMapper;

    public void startScheduleTask() {
        List<NodePO> nodes = nodeMapper.selectList(Condition.empty());
        if (nodes.size() > 0) {
            for (NodePO node : nodes) {
                addScheduleTask(node.getId());
            }
        }
    }

    public void stopScheduleTask() {
        SCHEDULE_TASK_FLAG = false;
    }

    public void addScheduleTask(int nodeId) {
        ScheduleTask task = new ScheduleTask(nodeId);
        synchronized (TASK_MAP) {
            TASK_MAP.put(nodeId, task);
        }
        jvmmConnectorFactory.getGlobalGroup().next().execute(task);
    }

    public void remScheduleTask(int nodeId) {
        ScheduleTask task = TASK_MAP.get(nodeId);
        if (task != null) {
            task.terminate();
            synchronized (TASK_MAP) {
                TASK_MAP.remove(nodeId);
            }
        }
    }

    class ScheduleTask implements Runnable {
        private static final int DEFAULT_FREQUENCY = 10;
        private final int nodeId;
        private volatile boolean TASK_FLAG = true;

        public ScheduleTask(int nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public void run() {
            int frequency = -1;
            try {
                NodePO node = nodeMapper.selectById(nodeId);
                if (node != null) {
                    NodeConfPO nodeConf = nodeConfMapper.selectById(nodeId);
                    frequency = nodeConf.getFrequency();
                    if (frequency <= 0) {
                        frequency = DEFAULT_FREQUENCY;
                    }
                    if (nodeConf.isAuto()) {
                        return;
                    }
                    JsonArray items = packPickItems(nodeConf);
                    if (items.size() == 0) {
                        return;
                    }
                    JvmmConnector connector = jvmmConnectorFactory.getConnector(node);
                    JvmmConnector.MsgReceiveListener listener = new JvmmConnector.MsgReceiveListener() {
                        @Override
                        public void onMessage(JvmmResponse rsp) {
                            try {
                                if (Objects.equals(rsp.getStatus(), GlobalStatus.JVMM_STATUS_OK.name())) {
                                    trySendWebsocket(rsp.getData());
                                    if (nodeConf.isStore()) {
                                        JsonObject data = rsp.getData().getAsJsonObject();
                                        storeLog(data);
                                    }
                                }
                            } finally {
                                connector.removeListener(this);
                            }
                        }
                    };
                    connector.registerListener(listener);
                    ChannelFuture future = connector.send(JvmmRequest.create().setType(GlobalType.JVMM_TYPE_COLLECT_BATCH).setData(items));
                    future.addListener(f -> {
                        if (!f.isSuccess()) {
                            connector.removeListener(listener);
                        }
                    });
                }
            } catch (TimeoutException e) {
                log.error("Error executing scheduled task: " + e.getMessage(), e);
            } finally {
                if (frequency > 0 && TASK_FLAG && SCHEDULE_TASK_FLAG) {
                    jvmmConnectorFactory.getGlobalGroup().next().schedule(this, frequency, TimeUnit.SECONDS);
                }
            }
        }

        private JsonArray packPickItems(NodeConfPO nodeConf) {
            JsonArray items = new JsonArray();
            if (nodeConf.isPickClassloading()) {
                items.add("classloading");
            }
            if (nodeConf.isPickGc()) {
                items.add("gc");
            }
            if (nodeConf.isPickMemory()) {
                items.add("memory");
            }
            if (nodeConf.isPickSystem()) {
                items.add("system");
            }
            if (nodeConf.isPickThread()) {
                items.add("thread");
            }
            return items;
        }

        private void trySendWebsocket(JsonElement data) {

        }

        private void storeLog(JsonObject data) {

        }

        public void terminate() {
            TASK_FLAG = false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ScheduleTask that = (ScheduleTask) o;

            return nodeId == that.nodeId;
        }

        @Override
        public int hashCode() {
            return nodeId;
        }
    }
}
