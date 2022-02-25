package org.beifengtz.jvmm.web.manage.factory;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import org.beifengtz.jvmm.common.exception.AuthenticationFailedException;
import org.beifengtz.jvmm.common.factory.ExecutorFactory;
import org.beifengtz.jvmm.convey.channel.JvmmChannelInitializer;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.beifengtz.jvmm.web.entity.po.NodePO;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.gson.internal.$Gson$Preconditions.checkArgument;

/**
 * Description: TODO
 *
 * Created in 15:44 2022/1/12
 *
 * @author beifengtz
 */
@Component
public class JvmmConnectorFactory {
    private static final EventLoopGroup GLOBAL_GROUP = JvmmChannelInitializer.newEventLoopGroup(4, ExecutorFactory.getScheduleThreadPool());

    private final Map<String, JvmmConnector> connectorPool = new ConcurrentHashMap<>(2);
    private final Map<String, Object> addressLock = new ConcurrentHashMap<>(2);

    public JvmmConnector getConnector(NodePO node) throws TimeoutException, AuthenticationFailedException {
        return getConnector(null, node);
    }

    public JvmmConnector getConnector(EventLoopGroup group, NodePO node) throws TimeoutException, AuthenticationFailedException {
        return getConnector(group, node.getIp() + ":" + node.getPort(),
                node.isAuthEnable() ? node.getAuthName() : null,
                node.isAuthEnable() ? node.getAuthPass() : null);
    }

    public JvmmConnector getConnector(String address, String authAccount, String authPassword)
            throws TimeoutException, AuthenticationFailedException {
        return getConnector(null, address, authAccount, authPassword);
    }

    public JvmmConnector getConnector(EventLoopGroup group, String address, String authAccount, String authPassword)
            throws TimeoutException, AuthenticationFailedException {
        checkArgument(address != null);

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
            connector = JvmmConnector.newInstance(split[0], Integer.parseInt(split[1]), group == null ? GLOBAL_GROUP : group, true, authAccount, authPassword);
            Future<Boolean> future = connector.connect();
            if (future.awaitUninterruptibly(3, TimeUnit.SECONDS)) {
                Boolean success = future.getNow();
                if (success != null && success) {
                    connectorPool.put(address, connector);
                    return connector;
                } else {
                    throw new AuthenticationFailedException();
                }
            } else {
                throw new TimeoutException("Connect jvmm server time out: " + address);
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
