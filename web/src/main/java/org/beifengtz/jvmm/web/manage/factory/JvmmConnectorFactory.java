package org.beifengtz.jvmm.web.manage.factory;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import org.beifengtz.jvmm.common.exception.AuthenticationFailedException;
import org.beifengtz.jvmm.common.factory.ExecutorFactory;
import org.beifengtz.jvmm.common.util.AssertUtil;
import org.beifengtz.jvmm.convey.channel.ChannelUtil;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.beifengtz.jvmm.web.entity.po.NodePO;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Description: TODO
 *
 * Created in 15:44 2022/1/12
 *
 * @author beifengtz
 */
@Component
public class JvmmConnectorFactory {
    private static final EventLoopGroup GLOBAL_GROUP = ChannelUtil.newEventLoopGroup(ExecutorFactory.getNThreads(), ExecutorFactory.getThreadPool());

    private final Map<String, JvmmConnector> connectorPool = new ConcurrentHashMap<>(2);
    private final Map<String, Object> addressLock = new ConcurrentHashMap<>(2);

    public JvmmConnector getConnector(NodePO node) throws Exception {
        return getConnector(null, node);
    }

    public JvmmConnector getConnector(EventLoopGroup group, NodePO node) throws Exception {
        return getConnector(group, node.getIp() + ":" + node.getPort(),
                node.isAuthEnable() ? node.getAuthName() : null,
                node.isAuthEnable() ? node.getAuthPass() : null);
    }

    public JvmmConnector getConnector(String address, String authAccount, String authPassword)
            throws Exception {
        return getConnector(null, address, authAccount, authPassword);
    }

    public JvmmConnector getConnector(EventLoopGroup group, String address, String authAccount, String authPassword)
            throws Exception {
        AssertUtil.checkArguments(address != null, "Missing required address");

        JvmmConnector connector = connectorPool.get(address);
        if (connector != null && connector.isConnected()) {
            return connector;
        }

        Object lock;
        if (!addressLock.containsKey(address)) {
            addressLock.putIfAbsent(address, new Object());
        }
        lock = addressLock.get(address);

        synchronized (lock) {
            String[] split = address.split(":");
            connector = JvmmConnector.newInstance(split[0], Integer.parseInt(split[1]), group == null ? GLOBAL_GROUP : group,
                    true, authAccount, authPassword);

            CompletableFuture<Boolean> future = connector.connect();
            Boolean success = future.get(3, TimeUnit.SECONDS);
            if (success) {
                connectorPool.put(address, connector);
                return connector;
            } else {
                throw new AuthenticationFailedException();
            }
        }
    }

    public synchronized void closeAll() {
        Iterator<Map.Entry<String, JvmmConnector>> it = connectorPool.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, JvmmConnector> entry = it.next();
            JvmmConnector connector = entry.getValue();
            if (connector.isConnected()) {
                connector.close();
            }
            it.remove();
        }
    }

    public void close(String address) {
        Object lock;
        if (!addressLock.containsKey(address)) {
            addressLock.putIfAbsent(address, new Object());
        }
        lock = addressLock.get(address);
        synchronized (lock) {
            JvmmConnector connector = connectorPool.get(address);
            if (connector != null && connector.isConnected()) {
                connector.close();
            }
            connectorPool.remove(address);
        }
    }

    public EventLoopGroup getGlobalGroup() {
        return GLOBAL_GROUP;
    }
}
