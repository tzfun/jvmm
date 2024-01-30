package org.beifengtz.jvmm.convey.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.factory.ExecutorFactory;
import org.beifengtz.jvmm.convey.channel.ChannelUtil;
import org.beifengtz.jvmm.convey.channel.HttpClientChannelInitializer;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description: TODO
 * date: 19:07 2024/1/29
 *
 * @author beifengtz
 */
public class HttpUtil {
    private static final byte[] BYTES_EMPTY = new byte[0];

    public static CompletableFuture<byte[]> asyncReq(EventLoopGroup group, FullHttpRequest req, int connectTimeoutMillis) {
        CompletableFuture<byte[]> completableFuture = new CompletableFuture<>();
        try {
            URI uri = new URI(req.uri());
            String scheme = Optional.ofNullable(uri.getScheme()).orElse("http");
            String host = Optional.ofNullable(uri.getHost()).orElse("127.0.0.1");
            int port = uri.getPort();
            SslContext sslCtx = null;
            if (port == -1) {
                if ("http".equalsIgnoreCase(scheme)) {
                    port = 80;
                } else if ("https".equalsIgnoreCase(scheme)) {
                    port = 443;
                }
            }
            if ("https".equalsIgnoreCase(scheme)) {
                sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            }
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new Exception("Only http(s) protocol is supported");
            }
            req.headers().set(HttpHeaderNames.HOST, uri.getHost());
            req.headers().set(HttpHeaderNames.CONTENT_LENGTH, req.content().readableBytes());

            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(ChannelUtil.channelClass(group))
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
                    .handler(new HttpClientChannelInitializer(new HttpHandlerProvider(completableFuture), sslCtx));
            ChannelFuture future = b.connect(host, port);
            future.addListener(f -> {
                if (f.isSuccess()) {
                    try {
                        Channel channel = future.channel();
                        if (channel.isWritable() && req.refCnt() > 0) {
                            channel.writeAndFlush(req);
                        } else {
                            completableFuture.completeExceptionally(new IOException("Send error: channel is not writable or request is released"));
                        }
                    } catch (Throwable t) {
                        completableFuture.completeExceptionally(t);
                    }
                } else {
                    completableFuture.completeExceptionally(f.cause());
                }
            });
        } catch (Exception e) {
            completableFuture.completeExceptionally(e);
        }
        return completableFuture;
    }

    public static CompletableFuture<byte[]> asyncReq(FullHttpRequest req) {
        return asyncReq(ExecutorFactory.getIOThreadPool(), req, 3000);
    }

    public static FullHttpRequest packRequest(HttpMethod method, String url, byte[] data, Map<String, String> headers) {
        FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, url, data == null
                ? Unpooled.buffer(0)
                : Unpooled.copiedBuffer(data));
        if (headers != null) {
            headers.forEach((k, v) -> req.headers().set(k, v));
        }
        return req;
    }

    private static class HttpHandlerProvider implements HandlerProvider {

        private final CompletableFuture<byte[]> future;

        public HttpHandlerProvider(CompletableFuture<byte[]> future) {
            this.future = future;
        }

        @Override
        public ChannelHandler getHandler() {
            return new HttpClientHandler(future);
        }
    }

    private static class HttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

        private final CompletableFuture<byte[]> future;

        public HttpClientHandler(CompletableFuture<byte[]> future) {
            super(true);
            this.future = future;
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            future.completeExceptionally(new IOException("Channel inactive"));
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
            byte[] bytes = getData(msg);
            if (msg.status().code() == HttpResponseStatus.OK.code()) {
                future.complete(bytes);
            } else {
                String body = new String(bytes, StandardCharsets.UTF_8);
                logger().debug("Response wrong status {}. {} ", msg.status(), body);
                future.completeExceptionally(new IOException(msg.status().toString()));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            future.completeExceptionally(cause);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if (e.state() == IdleState.READER_IDLE) {
                    future.completeExceptionally(new IOException("take too long to get response " + ctx.channel().remoteAddress().toString()));
                    ctx.close();
                }
            }
        }

        protected InternalLogger logger() {
            return InternalLoggerFactory.getInstance(HttpClientHandler.class);
        }
    }

    public static byte[] getData(FullHttpMessage msg) {
        ByteBuf content = msg.content();
        int readable = content.readableBytes();
        if (readable == 0) {
            return BYTES_EMPTY;
        }
        byte[] bytes = new byte[readable];
        content.readBytes(bytes);
        return bytes;
    }

    /**
     * 从url中获取参数键值对。支持两种数组读取方式：
     * <url>
     * <li>1. http://jvmm.beifengtz.com?arr=1&arr=2&arr=3</li>
     * <li>2. http://jvmm.beifengtz.com?arr[]=1&arr[]=2&arr[]=3</li>
     * </url>
     *
     * @param uri url地址
     * @return 参数键值对，key为 String，value为 String 或者 List
     * @throws UnsupportedEncodingException url解码失败时抛出此异常
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseParams(URI uri) throws UnsupportedEncodingException {
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
                List<String> array = (List<String>) params.computeIfAbsent(key.substring(0, key.length() - 2), o -> new ArrayList<>());
                array.add(value);
            } else {
                if (params.containsKey(key)) {
                    Object presentValue = params.get(key);
                    List<String> array;
                    if (presentValue instanceof List) {
                        array = (List<String>) presentValue;
                    } else {
                        array = new ArrayList<>();
                        array.add((String) presentValue);
                    }
                    array.add(value);
                    params.put(key, array);
                } else {
                    params.put(key, value);
                }
            }
        }
        return params;
    }

    /**
     * 解析带路径参数的url，例如 /jvmm/query/info/{type}/{id}，需解析出其中的两个参数：type和id
     *
     * @param mode 解析模板，例如：/jvmm/query/info/{type}/{id}
     * @param uri  请求的路径 Path（不带参数Query），例如：/jvmm/query/info/jvm_thread/1220
     * @return 匹配到的参数键值对
     */
    public static Map<String, String> parseParamsWithMode(String mode, String uri) {
        Map<String, String> params = new HashMap<>();
        String patternStr = mode.replaceAll("\\{[a-zA-Z0-9%_-]+}", "([a-zA-Z0-9%_-]+)");
        Pattern pattern = Pattern.compile(patternStr);
        String uriMode = mode.replaceAll("\\{([a-zA-Z0-9%_-]+)}", "$1");
        Matcher modeMatcher = pattern.matcher(uriMode);
        Matcher uriMatcher = pattern.matcher(uri);
        if (modeMatcher.find() && uriMatcher.find()) {
            int idx = 1;
            int max = Math.min(modeMatcher.groupCount(), uriMatcher.groupCount());
            while (idx <= max) {
                String key = modeMatcher.group(idx);
                String value = uriMatcher.group(idx);
                params.put(key, value);
                idx++;
            }
        }
        return params;
    }
}
