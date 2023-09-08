# jvmm-convey

jvmm convey模块是一个独立的、轻量的、通用的网络通信开发模块，它只负责网络收发数据，不与jvmm核心逻辑耦合，
你甚至可以把它当成一个开发框架，像 Spring MVC 那样使用。

# 使用

如果你愿意尝试或者计划使用此模块来构建自己的轻量http server，那么请看完后面的内容。

> 内部由 netty 实现，因此你可能需要用到一些 netty 内部的工具类。一般用 netty 原生开发逻辑写起来都比较麻烦，如果你想快速的搭建一个Http服务
> 并且像 SpringBoot 那样注解式编程的使用netty，但又不想引入 SpringBoot 庞大的依赖，那么可以考虑一下这个轻量通信框架。

## 引入依赖

maven或gradle引入
```xml
<dependency>
    <groupId>io.github.tzfun.jvmm</groupId>
    <artifactId>jvmm-convey</artifactId>
    <version>${jvmm-version}</version>
</dependency>
```

## 定义Controller

定义Controller，就像 SpringBoot 那样定义函数去写逻辑就行了
```java
import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;

@HttpController
public class BaseController {
    
    @HttpRequest(value = "/")
    public String get(@RequestParam String key) {
        return "ok " + key;
    }
}
```

## 定义Handler

为了实现Filter、Aspect那样，以切面的方式处理每一个请求的逻辑，你需要定义一个Handler来专门处理这些逻辑，它需要继承自 HttpChannelHandler。

> 当前如果你想实现更强大的 Aspect 切面功能，可以考虑使用 [jvmm-aop](../aop/README.md) 模块

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpHandler extends HttpChannelHandler {

    @Override
    public Logger logger() {
        return LoggerFactory.getLogger(HttpHandler.class);
    }

    @Override
    protected boolean handleBefore(ChannelHandlerContext ctx, String uri, FullHttpRequest msg) {
        return true;
    }

    @Override
    protected boolean handleUnmapping(ChannelHandlerContext ctx, String path, FullHttpRequest msg) {
        return true;
    }

    @Override
    protected void handleException(ChannelHandlerContext ctx, FullHttpRequest req, Throwable e) {
        
    }
}

```

## 定义Service

最后一步，你需要定义一个 service 并启动它
```java
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import org.beifengtz.etcd.server.handler.HttpHandler;
import org.beifengtz.jvmm.convey.channel.ChannelUtil;
import org.beifengtz.jvmm.convey.channel.HttpServerChannelInitializer;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpService {

    protected Channel channel;

    public void start(int port) {
        EventLoopGroup boosGroup = ChannelInitializers.newEventLoopGroup(1);
        EventLoopGroup workGroup = ChannelInitializers.newEventLoopGroup(2);
        ChannelFuture future = new ServerBootstrap()
                .group(boosGroup, workGroup)
                .channel(ChannelInitializers.serverChannelClass(workGroup))
                .childHandler(new HttpServerChannelInitializer(new HandlerProvider() {
                    @Override
                    public ChannelHandler getHandler() {
                        return new HttpHandler();
                    }
                }))
                .bind(port)
                .syncUninterruptibly();

        channel = future.channel();
    }

    public static void main(String[] args) {
        new HttpService().start(8080);
    }
}
```

至此你就成功搭建完一个简单的Http Server了

# 注解使用

## @HttpController

无参数

被注解了 HttpController 的类将会被 HttpHandler 扫描，定义在其中的方法需要带有 HttpRequest 注解才可以被请求上下文所指定

## HttpRequest

注解参数：
* value：Http请求的上下文路径
* method：指定Http Method


被注解了 HttpRequest 的方法，且方法定义在注解有 HttpController 的类中，当接收到指定的请求时会触发此方法。
方法返回的值可以是任意可以被 Json 解析的值，也可以是基本数据类型，也可以是 null，如果返回null 此次处理将不会返回并断开，
而是交给方法去管理连接，这一般用于异步处理。

允许的方法参数：
* 带有 @RequestParam 的参数
* 带有 @RequestAttr 的参数
* 带有 @RequestBody 的参数
* io.netty.channel.Channel 获得此连接的Channel
* io.netty.channel.ChannelHandlerContext 对象，获得此连接上下文
* io.netty.util.concurrent.EventExecutor 对象，获得处理此连接的线程执行器
* io.netty.handler.codec.http.FullHttpRequest 对象，获得 Http 请求的完整信息
* org.beifengtz.jvmm.convey.handler.HttpChannelHandler 对象，获得处理此Controller的 handler
* org.beifengtz.jvmm.convey.entity.ResponseFuture 对象，获得一个异步返回的Future

示例：
```java
@HttpController
public class Controller {
    @HttpRequest(value = "/test/post", mthod = Method.POST)
    public String post() {
        return "post success";
    }
}
```

## @RequestParam

注解参数：
* value：url参数值，如果不定义默认使用变量名

```java
@HttpController
public class Controller {
    @HttpRequest("/test/get")
    public String get(@RequestParam String name) {
        return "get name " + name;
    }

    @HttpRequest("/test/get_arr")
    public String get(@RequestParam int[] ports) {
        return "get ports " + Arrays.toString(ports);
    }
}
```

## @RequestBody

无参数

获取Http Body数据，并按照变量类型解析

```java
@HttpController
public class Controller {
    @HttpRequest("/test/post")
    public String post(@RequestBody UserDTO user) {
        return user.getId();
    }
}
```

## @RequestAttr

注解参数：
* value：此次请求的属性名

此注解使用了 Netty 的 Attr ，因此注解的 value 就是定义的属性名，一般用于 Handler 处理后存储一些状态或者信息到上下文中，供后续逻辑处理。

handler中处理并设置attr
```java

public class HttpHandler extends HttpChannelHandler {
    @Override
    protected boolean handleBefore(ChannelHandlerContext ctx, String uri, FullHttpRequest msg) {
        //  ...
        ctx.channel().attr(AttributeKey.valueOf("sessionId")).set("session123123123");
        return true;
    }
}
```

```java
@HttpController
public class Controller {
    @HttpRequest("/test/get")
    public String get(@RequestAttr String sessionId) {
        return "get attr sessionId " + sessionId;
    }
}
```
