package org.beifengtz.jvmm.server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.EventExecutor;
import org.beifengtz.jvmm.common.JsonParsable;
import org.beifengtz.jvmm.common.exception.InvalidJvmmMappingException;
import org.beifengtz.jvmm.convey.GlobalStatus;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.handler.JvmmChannelHandler;
import org.beifengtz.jvmm.server.annotation.JvmmController;
import org.beifengtz.jvmm.server.annotation.JvmmMapping;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.util.ReflexUtil;
import org.slf4j.Logger;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 9:39 2021/5/18
 *
 * @author beifengtz
 */
public class ServerHandler extends JvmmChannelHandler {
    private static final Logger logger = LoggerFactory.logger(ServerHandler.class);

    private static final Set<Class<?>> controllers;
    private static final Map<String, Method> mappings;

    private final Map<Class<?>, Object> controllerInstance = new ConcurrentHashMap<>(controllers.size());

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

    @Override
    public void handleRequest(ChannelHandlerContext ctx, JvmmRequest reqMsg) {
        try {
            Method method = mappings.get(reqMsg.getType());

            if (method == null) {
                logger.warn("Unsupported message type: " + reqMsg.getType());
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
                    parameter[i] = ctx.channel();
                } else if (JvmmRequest.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqMsg;
                } else if (JsonElement.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqMsg.getData();
                } else if (String.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqMsg.getType();
                } else if (EventExecutor.class.isAssignableFrom(parameterType)) {
                    parameter[i] = ctx.executor();
                } else if (JsonElement.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqMsg.getData();
                } else if (ChannelHandlerContext.class.isAssignableFrom(parameterType)) {
                    parameter[i] = ctx;
                } else if (ServerHandler.class.isAssignableFrom(parameterType)) {
                    parameter[i] = this;
                } else {
                    parameter[i] = null;
                }
            }

            Object result = method.invoke(instance, parameter);
            if (result instanceof JvmmResponse) {
                ctx.channel().writeAndFlush(((JvmmResponse) result).serialize());
            } else if (result != null) {
                JvmmResponse response = JvmmResponse.create().setStatus(GlobalStatus.JVMM_STATUS_OK).setType(reqMsg.getType());
                JsonElement data = null;
                if (result instanceof Boolean) {
                    data = new JsonPrimitive((Boolean) result);
                } else if (result instanceof Number) {
                    data = new JsonPrimitive((Number) result);
                } else if (result instanceof String) {
                    data = new JsonPrimitive((String) result);
                } else if (result instanceof Character) {
                    data = new JsonPrimitive((Character) result);
                } else if (result instanceof JsonElement) {
                    data = (JsonElement) result;
                } else if (result instanceof JsonParsable) {
                    data = ((JsonParsable) result).toJson();
                } else {
                    data = new Gson().toJsonTree(result);
                }
                response.setData(data);
                ctx.channel().writeAndFlush(response.serialize());
            }
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                handleException(ctx, reqMsg, ((InvocationTargetException) e).getTargetException());
            } else {
                handleException(ctx, reqMsg, e);
            }
        }
    }

    @Override
    public void handleIdle(ChannelHandlerContext ctx) {
        ctx.close();
        logger.debug("Channel close by idle");
    }

    @Override
    public void cleanup(ChannelHandlerContext ctx) throws Exception {
        for (Object controller : controllerInstance.values()) {
            if (controller.getClass().isAssignableFrom(Closeable.class)) {
                ((Closeable) controller).close();
            }
        }
        controllerInstance.clear();
    }

    private void handleException(ChannelHandlerContext ctx, JvmmRequest req, Throwable e) {
        JvmmResponse response = JvmmResponse.create().setType(req.getType());
        if (e instanceof IllegalArgumentException) {
            logger().error(e.getMessage(), e);
            response.setStatus(GlobalStatus.JVMM_STATUS_ILLEGAL_ARGUMENTS);
        } else {
            logger().error(e.getMessage(), e);
            response.setStatus(GlobalStatus.JVMM_STATUS_SERVER_ERROR);
        }
        if (e.getMessage() != null) {
            response.setMessage(e.getMessage());
        }
        ctx.writeAndFlush(response.serialize());
    }

    @Override
    public Logger logger() {
        return logger;
    }

}
