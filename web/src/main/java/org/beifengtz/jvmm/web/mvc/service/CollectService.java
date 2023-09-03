package org.beifengtz.jvmm.web.mvc.service;

import com.baomidou.mybatisplus.mapper.Condition;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.enums.GlobalStatus;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.beifengtz.jvmm.core.entity.info.JvmClassLoadingInfo;
import org.beifengtz.jvmm.core.entity.info.JvmGCInfo;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryInfo;
import org.beifengtz.jvmm.core.entity.info.JvmMemoryPoolInfo;
import org.beifengtz.jvmm.core.entity.info.JvmThreadInfo;
import org.beifengtz.jvmm.web.entity.po.LogClassloadingPO;
import org.beifengtz.jvmm.web.entity.po.LogGcPO;
import org.beifengtz.jvmm.web.entity.po.LogMemoryPO;
import org.beifengtz.jvmm.web.entity.po.LogMemoryPoolPO;
import org.beifengtz.jvmm.web.entity.po.LogThreadPO;
import org.beifengtz.jvmm.web.entity.po.NodeConfPO;
import org.beifengtz.jvmm.web.entity.po.NodePO;
import org.beifengtz.jvmm.web.manage.factory.JvmmConnectorFactory;
import org.beifengtz.jvmm.web.mvc.dao.LogClassloadingMapper;
import org.beifengtz.jvmm.web.mvc.dao.LogGcMapper;
import org.beifengtz.jvmm.web.mvc.dao.LogMemoryMapper;
import org.beifengtz.jvmm.web.mvc.dao.LogMemoryPoolMapper;
import org.beifengtz.jvmm.web.mvc.dao.LogSystemMapper;
import org.beifengtz.jvmm.web.mvc.dao.LogThreadMapper;
import org.beifengtz.jvmm.web.mvc.dao.NodeConfMapper;
import org.beifengtz.jvmm.web.mvc.dao.NodeMapper;
import org.beifengtz.jvmm.web.mvc.handler.WebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.gson.internal.$Gson$Types.newParameterizedTypeWithOwner;

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
    @Resource
    private LogClassloadingMapper logClassloadingMapper;
    @Resource
    private LogGcMapper logGcMapper;
    @Resource
    private LogMemoryMapper logMemoryMapper;
    @Resource
    private LogMemoryPoolMapper logMemoryPoolMapper;
    @Resource
    private LogSystemMapper logSystemMapper;
    @Resource
    private LogThreadMapper logThreadMapper;

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

    public void refreshScheduleTask(int nodeId) {
        ScheduleTask task = TASK_MAP.get(nodeId);
        if (task == null || task.getState() == ScheduleTask.STATE_TERMINATED) {
            addScheduleTask(nodeId);
        }
    }

    class ScheduleTask implements Runnable {
        public static final int STATE_NEW = 1;
        public static final int STATE_RUNNING = 2;
        public static final int STATE_TERMINATED = 3;

        private static final int DEFAULT_FREQUENCY = 10;
        private final int nodeId;
        private int state;
        private volatile boolean TASK_FLAG = true;

        public ScheduleTask(int nodeId) {
            this.nodeId = nodeId;
            this.state = STATE_NEW;
        }

        @Override
        public void run() {
            int frequency = -1;
            try {
                state = STATE_RUNNING;
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
                            if (!Objects.equals(rsp.getType(), GlobalType.JVMM_TYPE_COLLECT_BATCH.name())) {
                                return;
                            }
                            try {
                                if (Objects.equals(rsp.getStatus(), GlobalStatus.JVMM_STATUS_OK.name())) {
                                    long now = System.currentTimeMillis();
                                    JsonObject data = rsp.getData().getAsJsonObject();
                                    trySendWebsocket(now, data);
                                    if (nodeConf.isStore()) {
                                        storeLog(now, data);
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
                log.error("Connect jvmm server timeout, node: {}", nodeId);
            } catch (Throwable e) {
                log.error("Error executing scheduled task: " + e.getMessage(), e);
            } finally {
                if (frequency > 0 && TASK_FLAG && SCHEDULE_TASK_FLAG) {
                    jvmmConnectorFactory.getGlobalGroup().next().schedule(this, frequency, TimeUnit.SECONDS);
                } else {
                    state = STATE_TERMINATED;
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
            if (nodeConf.isPickMemoryPool()) {
                items.add("memoryPool");
            }
            if (nodeConf.isPickSystem()) {
                items.add("system");
            }
            if (nodeConf.isPickThread()) {
                items.add("thread");
            }
            return items;
        }

        private void trySendWebsocket(long now, JsonObject data) {
            if (WebSocketHandler.hasSession(String.valueOf(nodeId))) {
                JsonObject notify = new JsonObject();
                notify.addProperty("type", "schedule");
                notify.addProperty("time", now);
                notify.add("data", data);
                try {
                    WebSocketHandler.send(notify.toString(), String.valueOf(nodeId));
                } catch (Exception e) {
                    log.error("Notify websocket failed: " + e.getMessage(), e);
                }
            }
        }

        @Transactional
        public void storeLog(long now, JsonObject data) {
            Gson gson = StringUtil.getGson();

            for (String key : data.keySet()) {
                if ("classloading".equals(key)) {
                    LogClassloadingPO po = new LogClassloadingPO();
                    po.merge(gson.fromJson(data.get(key), JvmClassLoadingInfo.class));
                    po.setCreateTime(now);
                    logClassloadingMapper.insert(po);
                } else if ("gc".equals(key)) {
                    LogGcPO po = new LogGcPO();
                    po.merge(gson.fromJson(data.get(key), JvmGCInfo.class));
                    po.setCreateTime(now);
                    logGcMapper.insert(po);
                } else if ("memory".equals(key)) {
                    LogMemoryPO po = new LogMemoryPO();
                    po.merge(gson.fromJson(data.get(key), JvmMemoryInfo.class));
                    po.setCreateTime(now);
                    logMemoryMapper.insert(po);
                } else if ("memoryPool".equals(key)) {
                    List<JvmMemoryPoolInfo> list = gson.fromJson(data.get(key), newParameterizedTypeWithOwner(List.class, JvmMemoryPoolInfo.class));
                    for (JvmMemoryPoolInfo info : list) {
                        LogMemoryPoolPO po = new LogMemoryPoolPO();
                        po.merge(info);
                        po.setCreateTime(now);
                        logMemoryPoolMapper.insert(po);
                    }
                } else if ("thread".equals(key)) {
                    LogThreadPO po = new LogThreadPO();
                    po.setCreateTime(now);
                    po.merge(gson.fromJson(data.get(key), JvmThreadInfo.class));
                    logThreadMapper.insert(po);
                }
            }
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

        public int getState() {
            return state;
        }
    }
}
