package org.beifengtz.jvmm.convey.handler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.EventExecutor;
import org.beifengtz.jvmm.common.JsonParsable;
import org.beifengtz.jvmm.common.exception.AuthenticationFailedException;
import org.beifengtz.jvmm.common.exception.InvalidJvmmMappingException;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.beifengtz.jvmm.common.util.CommonUtil;
import org.beifengtz.jvmm.common.util.ReflexUtil;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.common.util.meta.PairKey;
import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.RequestBody;
import org.beifengtz.jvmm.convey.annotation.RequestParam;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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

    static {
        controllers = ReflexUtil.scanAnnotation(getScanPack(), HttpController.class);
        mappings = new HashMap<>(controllers.size() * 5);
        methodMappings = new HashMap<>(controllers.size() * 5);

        Logger logger = LoggerFactory.logger(HttpChannelHandler.class);

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

    public static String getScanPack() {
        return "org.beifengtz.jvmm";
    }

    public void response(ChannelHandlerContext ctx, HttpResponseStatus status) {
        response(ctx, status, null, null);
    }

    public void response(ChannelHandlerContext ctx, HttpResponseStatus status, String data) {
        response(ctx, status, data == null ? null : data.getBytes(StandardCharsets.UTF_8), null);
    }

    public void response(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] data, Map<String, Object> headers) {
        HttpResponse resp;
        if (data == null) {
            resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        } else {
            resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(data));
            resp.headers().set("Content-Length", data.length);
        }

        if (headers != null && !headers.isEmpty()) {
            headers.forEach((k, v) -> resp.headers().set(k, v));
        }
        ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }

    public void response401(ChannelHandlerContext ctx) {
        response(ctx, HttpResponseStatus.UNAUTHORIZED, null, CommonUtil.hasMapOf("WWW-Authenticate", "Basic realm=\"Restricted Access\""));
    }

    public void response400(ChannelHandlerContext ctx, String msg) {
        response(ctx, HttpResponseStatus.BAD_REQUEST, msg);
    }

    public void response404(ChannelHandlerContext ctx) {
        response(ctx, HttpResponseStatus.NOT_FOUND);
    }

    public void response405(ChannelHandlerContext ctx) {
        response(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED, "Method not allowed");
    }

    public void response500(ChannelHandlerContext ctx, String msg) {
        response(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        PairKey<URI, String> pair = filterUri(ctx, msg);
        if (pair == null) {
            return;
        }
        try {
            Map<String, String> params = loadParam(pair.getLeft());

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

            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] parameter = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                if (Channel.class.isAssignableFrom(parameterType)) {
                    parameter[i] = ctx.channel();
                } else if (FullHttpRequest.class.isAssignableFrom(parameterType)) {
                    parameter[i] = null;
                } else if (parameterType.isAnnotationPresent(RequestBody.class)) {
                    byte[] body = getBody(msg);
                    if (String.class.isAssignableFrom(parameterType)) {
                        parameter[i] = body == null ? null : new String(body, StandardCharsets.UTF_8);
                    } else {
                        parameter[i] = body == null ? null : new Gson().fromJson(new String(body, StandardCharsets.UTF_8), parameterType);
                    }
                } else if (parameterType.isAnnotationPresent(RequestParam.class)) {
                    RequestParam rp = parameterType.getAnnotation(RequestParam.class);
                    String key = "".equals(rp.value()) ? method.getParameters()[i].getName() : rp.value();
                    String value = params.get(key);
                    if (parameterType.isAssignableFrom(String.class)) {
                        parameter[i] = value;
                    } else if (parameterType.isAssignableFrom(int.class)) {
                        parameter[i] = Integer.parseInt(value);
                    } else if (parameterType.isAssignableFrom(double.class)) {
                        parameter[i] = Double.parseDouble(value);
                    } else if (parameterType.isAssignableFrom(boolean.class)) {
                        parameter[i] = Boolean.parseBoolean(value);
                    } else if (parameterType.isAssignableFrom(long.class)) {
                        parameter[i] = Long.parseLong(value);
                    } else {
                        throw new IllegalArgumentException("Can not resolve param " + key);
                    }
                } else if (EventExecutor.class.isAssignableFrom(parameterType)) {
                    parameter[i] = ctx.executor();
                } else if (ChannelHandlerContext.class.isAssignableFrom(parameterType)) {
                    parameter[i] = ctx;
                } else if (getClass().isAssignableFrom(parameterType)) {
                    parameter[i] = this;
                } else {
                    parameter[i] = null;
                }
            }

            Object result = method.invoke(instance, parameter);

            if (result instanceof HttpResponse) {
                ctx.writeAndFlush(result).addListener(ChannelFutureListener.CLOSE);
            } else if (result != null) {
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
                response(ctx, HttpResponseStatus.OK, data.toString());
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

    private Map<String, String> loadParam(URI uri) throws UnsupportedEncodingException {
        String query = uri.getQuery();
        Map<String, String> params = new HashMap<>();
        if (query == null) {
            return params;
        }
        String[] split = query.split("&");
        for (String str : split) {
            if (!str.contains("=")) {
                continue;
            }
            String[] kv = str.split("=");
            if (kv.length > 1) {
                params.put(kv[0], URLDecoder.decode(kv[1], "UTF-8"));
            } else {
                params.put(kv[0], "");
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
            logger().error(e.getMessage(), e);
            response401(ctx);
        } else {
            logger().error(e.getMessage(), e);
            response500(ctx, e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
