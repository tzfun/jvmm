package org.beifengtz.jvmm.web.mvc.handler;

import lombok.extern.slf4j.Slf4j;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: Web socket service handler
 * </p>
 * <p>
 * Created in 2:23 下午 2022/1/11
 *
 * @author beifengtz
 */
@ServerEndpoint("/jvmm/ws/{socketKey}")
@Component
@Slf4j
public class WebSocketHandler {
    private static final String FLAG_CONNECT = "connected";
    public static final String FLAG_START = "start";
    public static final String FLAG_FINISH = "finished";

    private static final AtomicInteger onlineCount = new AtomicInteger();
    private static final ConcurrentHashMap<String, WebSocketHandler> webSocketMap = new ConcurrentHashMap<>();
    private Session session;
    private String socketKey = "";
    private final BlockingDeque<String> queue = new LinkedBlockingDeque<>();

    public static boolean hasSession(String socketKey) {
        return webSocketMap.containsKey(socketKey);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("socketKey") String socketKey) throws IOException {
        this.session = session;
        this.socketKey = socketKey;
        if (!webSocketMap.containsKey(socketKey)) {
            onlineCount.incrementAndGet();
        }
        webSocketMap.put(socketKey, this);
        sendMessage(FLAG_CONNECT);

        log.debug("{} opened websocket connection", socketKey);
    }

    @OnClose
    public void onClose() {
        webSocketMap.remove(socketKey);
        onlineCount.decrementAndGet();
        log.debug("{} closed websocket connection", socketKey);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("{} websocket error: {}", socketKey, error.getMessage(), error);
    }

    private synchronized void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    private synchronized void close() throws IOException {
        session.close();
    }

    public static void send(String message, @PathParam("socketKey") String socketKey) throws IOException {
        log.debug("{} send message: {}", socketKey, message);
        if (StringUtil.nonEmpty(socketKey) && webSocketMap.containsKey(socketKey)) {
            WebSocketHandler handler = webSocketMap.get(socketKey);
            handler.sendMessage(message);
            if (message.equals(FLAG_FINISH)) {
                handler.close();
            }
        }
    }

    public static void sendGracefully(String message, @PathParam("socketKey") String socketKey) {
        try {
            send(message, socketKey);
        } catch (Exception e) {
            log.error("Send web socket failed. {}:{}", e.getClass().getName(), e.getMessage());
        }
    }

    /**
     * 生成websocket返回结构体
     *
     * @param item    主体
     * @param success 是否成功
     * @param msg     信息
     * @return map结构
     */
    public static Map<String, Object> genResp(Object item, Serializable key, boolean success, String msg) {
        Map<String, Object> respMsg = new HashMap<>();
        respMsg.put("item", item);
        respMsg.put("key", key);
        respMsg.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        respMsg.put("success", success);
        if (success) {
            respMsg.put("info", msg);
        } else {
            respMsg.put("msg", msg);
        }
        return respMsg;
    }
}
