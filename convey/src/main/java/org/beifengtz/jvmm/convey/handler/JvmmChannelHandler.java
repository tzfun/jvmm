package org.beifengtz.jvmm.convey.handler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.beifengtz.jvmm.common.exception.AuthenticationFailedException;
import org.beifengtz.jvmm.common.exception.InvalidJvmmMappingException;
import org.beifengtz.jvmm.common.exception.InvalidMsgException;
import org.beifengtz.jvmm.common.util.ReflexUtil;
import org.beifengtz.jvmm.common.util.SystemPropertyUtil;
import org.beifengtz.jvmm.convey.annotation.JvmmController;
import org.beifengtz.jvmm.convey.annotation.JvmmMapping;
import org.beifengtz.jvmm.convey.auth.JvmmBubble;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleDecrypt;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleEncrypt;
import org.beifengtz.jvmm.convey.channel.ChannelInitializers;
import org.beifengtz.jvmm.convey.channel.JvmmServerChannelInitializer;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.entity.ResponseFuture;
import org.beifengtz.jvmm.convey.enums.GlobalStatus;
import org.beifengtz.jvmm.convey.enums.GlobalType;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
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
 * Created in 18:53 2021/5/17
 *
 * @author beifengtz
 */
public abstract class JvmmChannelHandler extends SimpleChannelInboundHandler<String> {

    protected final JvmmBubble bubble = new JvmmBubble();
    private final Map<Class<?>, Object> controllerInstance = new ConcurrentHashMap<>(controllers.size());
    private static final Set<Class<?>> controllers;
    private static final Map<String, Method> mappings;
    private static final ChannelGroup channels = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

    static {
        controllers = ReflexUtil.scanAnnotation(SystemPropertyUtil.get("jvmm.scanPack", "org.beifengtz.jvmm"), JvmmController.class);
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

    public static ChannelGroupFuture closeAllChannels() {
        return channels.close();
    }

    public JvmmChannelHandler() {
        super(true);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        int seed = bubble.generateSeed();
        JsonObject data = new JsonObject();
        data.addProperty("seed", seed);
        data.addProperty("key", bubble.getKey());

        JvmmResponse bubbleResp = JvmmResponse.create()
                .setType(GlobalType.JVMM_TYPE_BUBBLE)
                .setStatus(GlobalStatus.JVMM_STATUS_OK)
                .setData(data);
        ctx.writeAndFlush(bubbleResp.serialize());

        ctx.pipeline()
                .addAfter(ctx.executor(), ChannelInitializers.STRING_DECODER_HANDLER,
                        JvmmServerChannelInitializer.JVMM_BUBBLE_ENCODER, new JvmmBubbleEncrypt(seed, bubble.getKey()))
                .addAfter(ctx.executor(), ChannelInitializers.STRING_DECODER_HANDLER,
                        JvmmServerChannelInitializer.JVMM_BUBBLE_DECODER, new JvmmBubbleDecrypt(seed, bubble.getKey()));
        channels.add(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        for (Object controller : controllerInstance.values()) {
            if (controller.getClass().isAssignableFrom(Closeable.class)) {
                ((Closeable) controller).close();
            }
        }
        controllerInstance.clear();
        channels.remove(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        try {
            long startTime = System.currentTimeMillis();
            JvmmRequest request = JvmmRequest.parseFrom(msg);
            if (request.getType() == null) {
                return;
            }
            if (GlobalType.JVMM_TYPE_PING.name().equals(request.getType())) {
                ctx.channel().writeAndFlush(JvmmResponse.create()
                        .setType(GlobalType.JVMM_TYPE_PONG)
                        .setStatus(GlobalStatus.JVMM_STATUS_OK)
                        .serialize());
            } else {
                handleRequest(ctx, request);
            }
            if (!request.isHeartbeat()) {
                logger().debug("{} {} ms", request.getType(), System.currentTimeMillis() - startTime);
            }
        } catch (AuthenticationFailedException e) {
            ctx.channel().writeAndFlush(JvmmResponse.create()
                    .setType(GlobalType.JVMM_TYPE_AUTHENTICATION)
                    .setStatus(GlobalStatus.JVMM_STATUS_AUTHENTICATION_FAILED.name())
                    .setMessage("Authentication failed.")
                    .serialize());
            ctx.close();
            logger().debug("Channel closed by auth failed");
        } catch (JsonSyntaxException e) {
            ctx.channel().writeAndFlush(JvmmResponse.create()
                    .setType(GlobalType.JVMM_TYPE_HANDLE_MSG)
                    .setStatus(GlobalStatus.JVMM_STATUS_UNRECOGNIZED_CONTENT.name())
                    .setMessage(e.getMessage())
                    .serialize());
        } catch (Throwable e) {
            ctx.fireExceptionCaught(e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof InvalidMsgException) {
            logger().error("Invalid message verify, seed: {}, ip: {}", ((InvalidMsgException) cause).getSeed(), JvmmConnector.getIpByCtx(ctx));
            ctx.close();
            logger().debug("Channel closed by message verify");
        } else if (cause instanceof IOException) {
            logger().debug(cause.toString());
        } else if (cause instanceof TooLongFrameException) {
            ctx.close();
        } else {
            logger().error(cause.toString(), cause);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                ctx.close();
                logger().debug("Channel close by idle");
            }
        }
    }

    public abstract Logger logger();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void handleRequest(ChannelHandlerContext ctx, JvmmRequest reqMsg) {
        try {
            if (!handleBefore(ctx, reqMsg)) {
                return;
            }

            Method method = mappings.get(reqMsg.getType());

            if (method == null) {
                logger().warn("Unsupported message type: " + reqMsg.getType());
                return;
            }

            Class<?> controller = method.getDeclaringClass();
            Object instance = controllerInstance.computeIfAbsent(controller, o -> {
                try {
                    return controller.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] parameter = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                if (Channel.class.isAssignableFrom(parameterType)) {
                    parameter[i] = ctx.channel();
                } else if (JvmmRequest.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqMsg;
                } else if (JsonObject.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqMsg.getData() == null ? null : reqMsg.getData().getAsJsonObject();
                } else if (JsonArray.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqMsg.getData() == null ? null : reqMsg.getData().getAsJsonArray();
                } else if (JsonElement.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqMsg.getData();
                } else if (String.class.isAssignableFrom(parameterType)) {
                    JsonElement data = reqMsg.getData();
                    if (data == null) {
                        parameter[i] = null;
                    } else {
                        if (data.isJsonObject()) {
                            String paramName = method.getParameters()[i].getName();
                            JsonElement value = data.getAsJsonObject().get(paramName);
                            if (value == null) {
                                parameter[i] = null;
                            } else {
                                parameter[i] = value.getAsString();
                            }
                        } else if (data.isJsonPrimitive()) {
                            parameter[i] = data.getAsString();
                        } else {
                            parameter[i] = data.toString();
                        }
                    }
                } else if (EventExecutor.class.isAssignableFrom(parameterType)) {
                    parameter[i] = ctx.executor();
                } else if (ChannelHandlerContext.class.isAssignableFrom(parameterType)) {
                    parameter[i] = ctx;
                } else if (getClass().isAssignableFrom(parameterType)) {
                    parameter[i] = this;
                } else if (ResponseFuture.class.isAssignableFrom(parameterType)) {
                    parameter[i] = new ResponseFuture(data -> {
                        JvmmResponse response;
                        if (data instanceof JvmmResponse) {
                            response = (JvmmResponse) data;
                        } else {
                            response = JvmmResponse.create()
                                    .setStatus(GlobalStatus.JVMM_STATUS_OK)
                                    .setType(reqMsg.getType())
                                    .setData(HandlerProvider.parseResult2Json(data));
                        }
                        ctx.channel().writeAndFlush(response.serialize());
                    });
                } else if (parameterType.isEnum()) {
                    parameter[i] = Enum.valueOf((Class<? extends Enum>) parameterType, reqMsg.getData().toString());
                } else if (ReflexUtil.isBaseType(parameterType)) {
                    JsonElement data = reqMsg.getData();
                    if (data != null) {
                        if (data.isJsonPrimitive()) {
                            parameter[i] = ReflexUtil.parseValueFromStr(parameterType, data.getAsString());
                        } else if (data.isJsonObject()) {
                            String paramName = method.getParameters()[i].getName();
                            JsonElement value = data.getAsJsonObject().get(paramName);
                            if (value != null) {
                                parameter[i] = ReflexUtil.parseValueFromStr(parameterType, value.getAsString());
                            }
                        }
                    }
                    if (parameter[i] == null) {
                        parameter[i] = ReflexUtil.getBaseTypeDefault(parameterType);
                    }
                } else {
                    parameter[i] = new Gson().fromJson(reqMsg.getData(), method.getGenericParameterTypes()[i]);
                }
            }

            Object result = method.invoke(instance, parameter);
            if (result instanceof JvmmResponse) {
                ctx.channel().writeAndFlush(((JvmmResponse) result).serialize());
            } else if (result != null) {
                JvmmResponse response = JvmmResponse.create()
                        .setStatus(GlobalStatus.JVMM_STATUS_OK)
                        .setType(reqMsg.getType())
                        .setData(HandlerProvider.parseResult2Json(result));
                ctx.channel().writeAndFlush(response.serialize());
            }
        } catch (Throwable e) {
            if (e instanceof AuthenticationFailedException) {
                throw (AuthenticationFailedException) e;
            } else if (e instanceof InvocationTargetException) {
                handleException(ctx, reqMsg, ((InvocationTargetException) e).getTargetException());
            } else {
                handleException(ctx, reqMsg, e);
            }
        }
    }

    protected void handleException(ChannelHandlerContext ctx, JvmmRequest req, Throwable e) {
        JvmmResponse response = JvmmResponse.create().setType(req.getType());
        if (e instanceof IllegalArgumentException) {
            logger().error(e.getMessage(), e);
            response.setStatus(GlobalStatus.JVMM_STATUS_ILLEGAL_ARGUMENTS).setMessage(e.getMessage());
        } else {
            logger().error(e.getMessage(), e);
            response.setStatus(GlobalStatus.JVMM_STATUS_SERVER_ERROR);
        }
        if (e.getMessage() != null) {
            response.setMessage(e.getClass().getName() + ": " + e.getMessage());
        }
        ctx.writeAndFlush(response.serialize());
    }

    /**
     * 消息处理之前调用
     *
     * @param ctx    channel 上下文
     * @param reqMsg 请求消息{@link JvmmRequest}
     * @return true-继续处理消息 false-消息已被拦截，无需继续处理
     * @throws Exception 处理失败异常
     */
    protected abstract boolean handleBefore(ChannelHandlerContext ctx, JvmmRequest reqMsg) throws Exception;
}
