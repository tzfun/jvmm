package org.beifengtz.jvmm.convey.handler;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.beifengtz.jvmm.common.exception.AuthenticationFailedException;
import org.beifengtz.jvmm.common.exception.InvalidJvmmMappingException;
import org.beifengtz.jvmm.common.util.CommonUtil;
import org.beifengtz.jvmm.common.util.ReflexUtil;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.common.util.meta.PairKey;
import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.RequestBody;
import org.beifengtz.jvmm.convey.annotation.RequestParam;
import org.beifengtz.jvmm.convey.entity.JvmmResponse;
import org.beifengtz.jvmm.convey.entity.ResponseFuture;
import org.beifengtz.jvmm.convey.enums.GlobalStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:31 2022/9/13
 *
 * @author beifengtz
 */
public abstract class HttpChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final Map<Class<?>, Object> controllerInstance = new ConcurrentHashMap<>(controllers.size());
    private static final Set<Class<?>> controllers;
    private static final Map<String, Method> mappings;
    private static final Map<String, HttpMethod> methodMappings;
    private static final ChannelGroup channels = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

    static {
        controllers = ReflexUtil.scanAnnotation(getScanPack(), HttpController.class);
        mappings = new HashMap<>(controllers.size() * 5);
        methodMappings = new HashMap<>(controllers.size() * 5);

        Logger logger = LoggerFactory.getLogger(HttpChannelHandler.class);

        for (Class<?> controller : controllers) {
            Set<Method> methods = ReflexUtil.scanMethodAnnotation(controller, HttpRequest.class);
            for (Method method : methods) {
                HttpRequest request = method.getAnnotation(HttpRequest.class);
                String uri = request.value();
                if (mappings.containsKey(uri) || methodMappings.containsKey(uri)) {
                    throw new InvalidJvmmMappingException("There are duplicate jvmm http mapping: " + uri);
                }

                if (!Modifier.isPublic(method.getModifiers())) {
                    throw new InvalidJvmmMappingException("The method annotated with '@HttpRequest' in the controller must be public: " + method.getName());
                }

                logger.debug("Scanning http api: [{}] {}", request.method().name(), uri);
                mappings.put(uri, method);
                methodMappings.put(uri, request.method().getValue());
            }
        }
    }

    public HttpChannelHandler() {
        super(false);
    }

    public static ChannelGroupFuture closeAllChannels() {
        return channels.close();
    }

    public static String getScanPack() {
        return "org.beifengtz.jvmm";
    }

    protected void response(ChannelHandlerContext ctx, HttpResponseStatus status) {
        response(ctx, status, null, null);
    }

    protected void response(ChannelHandlerContext ctx, HttpResponseStatus status, String data) {
        response(ctx, status, data == null ? null : data.getBytes(StandardCharsets.UTF_8), null);
    }

    protected void response(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] data, Map<String, Object> headers) {
        HttpResponse resp;
        if (data == null) {
            resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        } else {
            resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(data));
            resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, data.length);
            resp.headers().set(HttpHeaderNames.CONTENT_ENCODING, "UTF-8");
            resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=utf-8");
        }

        if (headers != null && !headers.isEmpty()) {
            headers.forEach((k, v) -> resp.headers().set(k, v));
        }
        ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }

    protected void response401(ChannelHandlerContext ctx) {
        response(ctx, HttpResponseStatus.UNAUTHORIZED, null, CommonUtil.hasMapOf("WWW-Authenticate", "Basic realm=\"Restricted Access\""));
    }

    protected void response400(ChannelHandlerContext ctx, String msg) {
        response(ctx, HttpResponseStatus.BAD_REQUEST, msg);
    }

    protected void response404(ChannelHandlerContext ctx) {
        response(ctx, HttpResponseStatus.NOT_FOUND);
    }

    protected void response405(ChannelHandlerContext ctx) {
        response(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED, "Method not allowed");
    }

    protected void response500(ChannelHandlerContext ctx, String msg) {
        response(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channels.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channels.remove(ctx.channel());
    }

    @Override
    @SuppressWarnings({"unchecked","rawtypes"})
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        PairKey<URI, String> pair = filterUri(ctx, msg);
        if (pair == null) {
            return;
        }
        try {
            Map<String, Object> params = loadParam(pair.getLeft());

            boolean keepHandle = handleBefore(ctx, pair.getRight(), msg);
            if (!keepHandle) {
                ctx.close();
                return;
            }
            Method method = mappings.get(pair.getRight());
            Class<?> controller = method.getDeclaringClass();
            Object instance = controllerInstance.computeIfAbsent(controller, o -> {
                try {
                    return controller.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] parameter = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                if (Channel.class.isAssignableFrom(parameterType)) {
                    parameter[i] = ctx.channel();
                } else if (FullHttpRequest.class.isAssignableFrom(parameterType)) {
                    parameter[i] = null;
                } else if (parameterAnnotations[i].length > 0) {
                    for (Annotation anno : parameterAnnotations[i]) {
                        if (anno.annotationType() == RequestBody.class) {
                            byte[] body = getBody(msg);
                            if (String.class.isAssignableFrom(parameterType)) {
                                parameter[i] = body == null ? null : new String(body, StandardCharsets.UTF_8);
                            } else {
                                parameter[i] = body == null ? null : new Gson().fromJson(new String(body, StandardCharsets.UTF_8), method.getGenericParameterTypes()[i]);
                            }
                        } else if (anno.annotationType() == RequestParam.class) {
                            RequestParam rp = (RequestParam) anno;
                            String key = "".equals(rp.value()) ? method.getParameters()[i].getName() : rp.value();
                            Object value = params.get(key);

                            if (parameterType.isArray()) {
                                List<String> valueArr = (List<String>) params.get(key + "[]");
                                Class<?> componentType = parameterType.getComponentType();
                                Object array = Array.newInstance(componentType, valueArr.size());
                                for (int j = 0; j < valueArr.size(); j++) {
                                    Array.set(array, j, ReflexUtil.parseValueFromStr(componentType, valueArr.get(j)));
                                }
                                parameter[i] = array;
                            } else if (parameterType.isAssignableFrom(List.class)) {
                                List<String> valueArr = (List<String>) params.get(key + "[]");
                                Type componentType = ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
                                List list = new ArrayList(valueArr.size());
                                for (String s : valueArr) {
                                    list.add(ReflexUtil.parseValueFromStr((Class<?>) componentType, s));
                                }
                                parameter[i] = list;
                            } else {
                                parameter[i] = ReflexUtil.parseValueFromStr(parameterType, (String) value);
                                if (parameter[i] == null) {
                                    parameter[i] = new Gson().fromJson(value.toString(), parameterType);
                                }
                            }
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
                        if (data instanceof JvmmResponse) {
                            JvmmResponse resp = (JvmmResponse) data;
                            if (GlobalStatus.JVMM_STATUS_OK.name().equals(resp.getStatus())) {
                                response(ctx, HttpResponseStatus.OK, resp.getData().toString());
                            } else {
                                response(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, resp.getMessage());
                            }
                        } else {
                            response(ctx, HttpResponseStatus.OK, HandlerProvider.parseResult2Json(data).toString());
                        }
                    });
                } else {
                    parameter[i] = null;
                }
            }

            Object result = method.invoke(instance, parameter);

            if (result instanceof HttpResponse) {
                ctx.writeAndFlush(result).addListener(ChannelFutureListener.CLOSE);
            } else if (result != null) {
                response(ctx, HttpResponseStatus.OK, HandlerProvider.parseResult2Json(result).toString());
            }

        } catch (Exception e) {
            if (e instanceof AuthenticationFailedException) {
                throw (AuthenticationFailedException) e;
            } else if (e instanceof InvocationTargetException) {
                handleException(ctx, msg, ((InvocationTargetException) e).getTargetException());
            } else {
                handleException(ctx, msg, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadParam(URI uri) throws UnsupportedEncodingException {
        String query = uri.getQuery();
        Map<String, Object> params = new HashMap<>();
        if (query == null) {
            return params;
        }
        String[] split = query.split("&");
        for (String str : split) {
            if (!str.contains("=")) {
                continue;
            }
            String[] kv = str.split("=");
            if (kv.length <= 1) {
                continue;
            }
            String key = URLDecoder.decode(kv[0], "UTF-8");
            String value = URLDecoder.decode(kv[1], "UTF-8");
            if (key.endsWith("[]")) {
                List<String> array = (List<String>) params.computeIfAbsent(key, o -> new ArrayList<>());
                array.add(value);
            } else {
                params.put(key, value);
            }
        }
        return params;
    }

    private byte[] getBody(FullHttpRequest msg) {
        ByteBuf content = msg.content();
        if (content.readableBytes() == 0) return null;

        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);
        return bytes;
    }

    protected PairKey<URI, String> filterUri(ChannelHandlerContext ctx, FullHttpRequest msg) {
        try {
            URI uri = new URI(msg.uri());
            String path = uri.getPath();
            String apiPrefix = getApiPrefix();
            if (StringUtil.nonEmpty(apiPrefix)) {
                if (path.startsWith(apiPrefix)) {
                    path = path.replace(apiPrefix, "");
                } else {
                    response404(ctx);
                    return null;
                }
            }

            if (mappings.containsKey(path)) {
                if (msg.method().equals(HttpMethod.OPTIONS)) {
                    response(ctx, HttpResponseStatus.OK);
                    return null;
                }
                if (!msg.method().equals(methodMappings.get(path))) {
                    response405(ctx);
                    return null;
                }
            } else {
                response404(ctx);
                return null;
            }

            return PairKey.of(uri, path);
        } catch (URISyntaxException e) {
            response404(ctx);
            return null;
        }
    }

    public String getApiPrefix() {
        return null;
    }

    public abstract Logger logger();

    protected abstract boolean handleBefore(ChannelHandlerContext ctx, String uri, FullHttpRequest msg);

    protected void handleException(ChannelHandlerContext ctx, FullHttpRequest req, Throwable e) {
        if (e instanceof IllegalArgumentException) {
            response400(ctx, e.getMessage());
        } else {
            logger().error(e.getMessage(), e);
            response500(ctx, e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
