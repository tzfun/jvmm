package org.beifengtz.jvmm.server.channel;

import com.google.gson.JsonElement;
import io.netty.channel.Channel;
import org.beifengtz.jvmm.common.exception.InvalidJvmmMappingException;
import org.beifengtz.jvmm.convey.channel.JvmmSocketChannel;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.server.annotation.JvmmController;
import org.beifengtz.jvmm.server.annotation.JvmmMapping;
import org.beifengtz.jvmm.tools.util.ReflexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:27 2021/5/17
 *
 * @author beifengtz
 */
public class LogicSocketChannel extends JvmmSocketChannel {

    private static final Logger log = LoggerFactory.getLogger(LogicSocketChannel.class);

    private static final Set<Class<?>> controllers;
    private static final Map<String, Method> mappings;

    static {
        controllers = ReflexUtil.scanAnnotation("org.beifengtz.jvmm.server", JvmmController.class);
        mappings = new HashMap<>(controllers.size() * 5);

        for (Class<?> controller : controllers) {
            Set<Method> methods = ReflexUtil.scanMethodAnnotation(controller, JvmmMapping.class);
            for (Method method : methods) {
                JvmmMapping mapping = method.getAnnotation(JvmmMapping.class);
                String type = mapping.type();
                if ("".equals(type)) {
                    type = mapping.typeEnum().name();
                }
                if (mappings.containsKey(type)) {
                    throw new InvalidJvmmMappingException("There are duplicate jvmm mapping: " + type);
                }

                if (!Modifier.isPublic(method.getModifiers())) {
                    throw new InvalidJvmmMappingException("The method annotated with '@JvmmMapping' in the controller must be public: " + method.getName());
                }
                mappings.put(type, method);
            }
        }
    }

    private final Map<Class<?>, Object> controllerInstance = new ConcurrentHashMap<>(controllers.size());

    public LogicSocketChannel(ServerLogicSocketChannel parent, SocketChannel ch) {
        super(parent, ch);
    }

    @Override
    public void handleRequest(JvmmRequest reqMsg) {
        try {
            Method method = mappings.get(reqMsg.getType());

            if (method == null) {
                log.warn("Unsupported message type: " + reqMsg.getType());
                return;
            }

            Class<?> controller = method.getDeclaringClass();
            Object instance = controllerInstance.get(controller);
            if (instance == null) {
                instance = controller.newInstance();
                controllerInstance.put(controller, instance);
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] parameter = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                if (Channel.class.isAssignableFrom(parameterType)) {
                    parameter[i] = this;
                } else if (JvmmRequest.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqMsg;
                } else if (JsonElement.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqMsg.getData();
                } else if (String.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqMsg.getType();
                } else {
                    parameter[i] = null;
                }
            }

            Object result = method.invoke(instance, parameter);
            if (result instanceof JvmmResponse) {
                writeAndFlush(((JvmmResponse) result).serialize());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void handleIdle() {
        close();
    }

    @Override
    public void cleanup() {
        controllerInstance.clear();
    }
}
