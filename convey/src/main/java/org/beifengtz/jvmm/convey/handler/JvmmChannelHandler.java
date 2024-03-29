package org.beifengtz.jvmm.convey.handler;

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
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.internal.logging.InternalLogger;
import org.beifengtz.jvmm.common.exception.AuthenticationFailedException;
import org.beifengtz.jvmm.common.exception.InvalidJvmmMappingException;
import org.beifengtz.jvmm.common.exception.InvalidMsgException;
import org.beifengtz.jvmm.common.util.ReflexUtil;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.common.util.SystemPropertyUtil;
import org.beifengtz.jvmm.convey.annotation.JvmmController;
import org.beifengtz.jvmm.convey.annotation.JvmmMapping;
import org.beifengtz.jvmm.convey.auth.JvmmBubble;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleDecoder;
import org.beifengtz.jvmm.convey.auth.JvmmBubbleEncoder;
import org.beifengtz.jvmm.convey.channel.ChannelUtil;
import org.beifengtz.jvmm.convey.channel.JvmmServerChannelInitializer;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.entity.ResponseFuture;
import org.beifengtz.jvmm.convey.enums.RpcStatus;
import org.beifengtz.jvmm.convey.enums.RpcType;

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
public abstract class JvmmChannelHandler extends SimpleChannelInboundHandler<JvmmRequest> {

    protected final JvmmBubble bubble = new JvmmBubble();
    private final Map<Class<?>, Object> controllerInstance = new ConcurrentHashMap<>(controllers.size());
    private static Set<Class<?>> controllers;
    private static Map<RpcType, Method> mappings;
    private static final ChannelGroup channels = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

    public static void init() {
        String scanPackage = SystemPropertyUtil.get(SystemPropertyUtil.PROPERTY_JVMM_SCAN_PACKAGE, "org.beifengtz.jvmm");
        controllers = ReflexUtil.scanAnnotation(scanPackage, JvmmController.class);
        mappings = new HashMap<>(controllers.size() * 5);

        for (Class<?> controller : controllers) {
            Set<Method> methods = ReflexUtil.scanMethodAnnotation(controller, JvmmMapping.class);
            for (Method method : methods) {
                JvmmMapping mapping = method.getAnnotation(JvmmMapping.class);
                RpcType type = mapping.value();
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
                .setType(RpcType.JVMM_BUBBLE)
                .setStatus(RpcStatus.JVMM_STATUS_OK)
                .setData(data);
        ctx.writeAndFlush(bubbleResp);

        ctx.pipeline()
                .addBefore(ctx.executor(), ChannelUtil.JVMM_ENCODER,
                        JvmmServerChannelInitializer.JVMM_BUBBLE_ENCODER, new JvmmBubbleEncoder(seed, bubble.getKey()))
                .addBefore(ctx.executor(), ChannelUtil.JVMM_DECODER,
                        JvmmServerChannelInitializer.JVMM_BUBBLE_DECODER, new JvmmBubbleDecoder(seed, bubble.getKey()));
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
    protected void channelRead0(ChannelHandlerContext ctx, JvmmRequest request) throws Exception {
        try {
            long startTime = System.currentTimeMillis();
            if (request.getType() == null) {
                return;
            }
            if (RpcType.JVMM_PING.equals(request.getType())) {
                ctx.channel().writeAndFlush(JvmmResponse.create()
                        .setType(RpcType.JVMM_PING)
                        .setStatus(RpcStatus.JVMM_STATUS_OK)
                        .setMessage("pong")
                        .setContextId(request.getContextId()));
            } else {
                handleRequest(ctx, request);
            }
            if (request.getType() != RpcType.JVMM_HEARTBEAT) {
                logger().debug("{} {} ms", request.getType(), System.currentTimeMillis() - startTime);
            }
        } catch (AuthenticationFailedException e) {
            ctx.channel().writeAndFlush(JvmmResponse.create()
                    .setType(RpcType.JVMM_AUTHENTICATION)
                    .setStatus(RpcStatus.JVMM_STATUS_AUTHENTICATION_FAILED)
                    .setMessage("Authentication failed.")
                    .setContextId(request.getContextId()));
            ctx.close();
            logger().debug("Channel closed by auth failed");
        } catch (JsonSyntaxException e) {
            ctx.channel().writeAndFlush(JvmmResponse.create()
                    .setType(RpcType.JVMM_HANDLE_MSG)
                    .setStatus(RpcStatus.JVMM_STATUS_UNRECOGNIZED_CONTENT)
                    .setMessage(e.getMessage())
                    .setContextId(request.getContextId()));
        } catch (Throwable e) {
            ctx.fireExceptionCaught(e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof InvalidMsgException) {
            logger().error("Invalid message verify, seed: {}, ip: {}",
                    ((InvalidMsgException) cause).getSeed(), ChannelUtil.getIpByCtx(ctx));
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

    public abstract InternalLogger logger();

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
            JsonElement reqData = reqMsg.getData();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                String argName = method.getParameters()[i].getName();

                if (Channel.class.isAssignableFrom(parameterType)) {
                    parameter[i] = ctx.channel();
                } else if (JvmmRequest.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqMsg;
                } else if (JsonObject.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqData == null ? null : reqData.getAsJsonObject();
                } else if (JsonArray.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqData == null ? null : reqData.getAsJsonArray();
                } else if (JsonElement.class.isAssignableFrom(parameterType)) {
                    parameter[i] = reqData;
                } else if (String.class.isAssignableFrom(parameterType)) {
                    if (reqData == null) {
                        parameter[i] = null;
                    } else {
                        if (reqData.isJsonObject()) {
                            JsonElement value = reqData.getAsJsonObject().get(argName);
                            if (value == null) {
                                parameter[i] = null;
                            } else {
                                parameter[i] = value.getAsString();
                            }
                        } else if (reqData.isJsonPrimitive()) {
                            parameter[i] = reqData.getAsString();
                        } else {
                            parameter[i] = reqData.toString();
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
                                    .setStatus(RpcStatus.JVMM_STATUS_OK)
                                    .setType(reqMsg.getType())
                                    .setData(HandlerProvider.parseResult2Json(data))
                                    .setContextId(reqMsg.getContextId());
                        }
                        ctx.channel().writeAndFlush(response);
                    });
                } else if (parameterType.isEnum()) {
                    parameter[i] = Enum.valueOf((Class<? extends Enum>) parameterType, reqData.toString());
                } else if (ReflexUtil.isBaseType(parameterType)) {
                    if (reqData != null) {
                        if (reqData.isJsonPrimitive()) {
                            parameter[i] = ReflexUtil.parseValueFromStr(parameterType, reqData.getAsString());
                        } else if (reqData.isJsonObject()) {
                            String paramName = method.getParameters()[i].getName();
                            JsonElement value = reqData.getAsJsonObject().get(paramName);
                            if (value != null) {
                                parameter[i] = ReflexUtil.parseValueFromStr(parameterType, value.getAsString());
                            }
                        }
                    }
                    if (parameter[i] == null) {
                        parameter[i] = ReflexUtil.getBaseTypeDefault(parameterType);
                    }
                } else {
                    if (reqData == null) {
                        parameter[i] = null;
                        continue;
                    }

                    JsonElement json = reqData;
                    if (reqData.isJsonObject()) {
                        JsonElement paramData = reqData.getAsJsonObject().get(argName);
                        if (paramData != null) {
                            json = paramData;
                        }
                    }
                    parameter[i] = StringUtil.getGson().fromJson(json, method.getGenericParameterTypes()[i]);
                }
            }

            Object result = method.invoke(instance, parameter);
            Channel channel = ctx.channel();
            if (channel.isActive() && channel.isWritable()) {
                if (result instanceof JvmmResponse) {
                    channel.writeAndFlush(result);
                } else if (result != null) {
                    JvmmResponse response = JvmmResponse.create()
                            .setStatus(RpcStatus.JVMM_STATUS_OK)
                            .setType(reqMsg.getType())
                            .setData(HandlerProvider.parseResult2Json(result))
                            .setContextId(reqMsg.getContextId());
                    channel.writeAndFlush(response);
                }
            } else {
                logger().debug("Can not response because of channel is inactive or un-writable {}", channel);
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
        JvmmResponse response = JvmmResponse.create()
                .setType(req.getType())
                .setContextId(req.getContextId());
        if (e instanceof IllegalArgumentException) {
            logger().warn("Handle jvmm request failed: " + e.getMessage(), logger().isDebugEnabled() ? e : null);
            response.setStatus(RpcStatus.JVMM_STATUS_ILLEGAL_ARGUMENTS).setMessage(e.getMessage());
        } else {
            logger().error(e.getMessage(), e);
            response.setStatus(RpcStatus.JVMM_STATUS_SERVER_ERROR);
        }
        if (e.getMessage() != null) {
            response.setMessage(e.getMessage());
        }
        ctx.channel().writeAndFlush(response);
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
