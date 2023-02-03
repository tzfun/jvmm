server模块是基于[core模块](../core/README.md)开发的对外提供API服务的程序，如果你不想通过[Java Agent方式](../agent/README.md)启动Jvmm服务，可以直接在你的项目中使用它。

# 启动方式

提供三种方式启动一个Jvmm Server：独立启动、载入Java程序启动、项目中引入依赖

## 独立启动

如果你的监控对象更专注于操作系统或者物理机，独立启动方式可能比较适合你的应用场景。启动一个独立的Jvmm Server，并对外提供查询OS的内存、磁盘、网卡、进程等信息。

server jar包支持直接运行启动，你需要先生成`jvmm-server.jar`包，具体生成方法请前往[Jvmm客户端Jar模式生成Agent Jar](../client/README.md#依赖生成模式)

生成的`jvmm-server.jar`就是我们独立启动的目标jar包，它可以直接运行：

```shell
java -jar jvmm-server.jar ./config.yml
```

运行时可以指定一个[配置文件](#配置)，如果你不指定将会按以下顺序寻找配置：

1. jvmm-server.jar包同级目录下的`config.yml`文件
2. jvmm-server.jar包`config`目录下的`config.yml`文件
3. 如果以上都没找到则使用默认配置

**注意！如果你的JDK版本是9以上，需要设置下面两个虚拟机参数：**

```shell
# JDK 9+开始不允许动态加载依赖，需要设置以下两个虚拟机参数
# --add-opens java.base/jdk.internal.loader=ALL-UNNAMED
# --add-opens jdk.zipfs/jdk.nio.zipfs=ALL-UNNAMED
java -jar --add-opens java.base/jdk.internal.loader=ALL-UNNAMED --add-opens jdk.zipfs/jdk.nio.zipfs=ALL-UNNAMED jvmm-server.jar ./config.yml
```

## 载入Java程序启动

如果你的监控对象更专注于某一个Java程序，比如想监控JVM的内存使用情况、GC、线程状态、Class加载情况等，亦或是想对某一个Java程序做性能分析（火焰图）、代码热更、代码反编译等，此种方式可能比较适合你的应用场景。

载入Java程序你需要使用Agent辅助使用，Jvmm Agent提供两种方式载入：

* [运行时premain方式载入Jvmm Server](../agent/README.md#premain方式)
* [启动时agentmain方式载入Jvmm Server](../agent/README.md#agentmain方式)

**注意：只有Agent方式载入Java程序启动才能实现代码热更、代码反编译功能！**

## 项目中引入依赖启动

如果你的监控对象更专注于自己的Java程序，同[载入Java程序启动](#载入Java程序启动)一样，但你不想用agent的方式启动，或者是你的项目是一个多模块项目，Jvmm作为项目的监控程序，想一次引入多模块生效，那么引入依赖方式启动会更加适合你的应用场景。

你可以有两个选择在自己的项目中使用Jvmm的依赖：

* 直接引入`jvmm-server`依赖，直接调用`server.start()`启动，它的优缺点如下：
  * **优点**：简单快捷，只需调用**一行**启动代码就可实现三种服务的启动：jvmm、http、sentinel。
  * **缺点**：
    1. 内置了RPC模块（netty实现），如果你的项目也依赖了netty，可能有依赖版本冲突的问题。
    2. server的日志系统为了兼容agent模式，日志输出做了代理实现，当你的Jvmm Server日志输出量比较大时，可能会造成性能问题。（经过测试如果你的jvmm请求频次在**100ms**以上是没有影响的，在**100ms**内可能会有性能问题，但一般场景谁会那么频繁的去请求监控数据呢是吧，并且不是所有接口都会输出日志）。
* 引入`jvmm-core`依赖，调用API，使用自己项目中的RPC模块提供服务，它的优缺点如下：
  * **优点**：可完全自定义实现监控，监控项目及数据更加完整，没有RPC模块和日志系统，可以更优雅的嵌入你的项目。
  * **缺点**：开发难度较高，需阅读各个API的使用方法。

下面是引入`jvmm-server`依赖的使用文档，引入jvmm-core的方法请见[core模块文档](../core/README.md)

**注意：这种方式无法使用代码热更、代码反编译功能！**

### 依赖引入

Maven引入

```xml

<dependency>
    <groupId>io.github.tzfun.jvmm</groupId>
    <artifactId>jvmm-server</artifactId>
    <version>${jvmm-version}</version>
</dependency>
```

或 gradle引入

```groovy
implementation "io.github.tzfun.jvmm:jvmm-server:${jvmm-version}"
```

### 示例代码

```java
public class ServerBootDemo {
    public static void main(String[] args) {
        ServerBootstrap.getInstance(Configuration.parseFromUrl("./config.yml")).start();
    }
}
```

### 配置

你可以像示例代码中那样通过配置文件的方式进行配置，全部配置文件内容如下，默认配置请见[config.yml](../server/src/main/resources/config.yml)。

```yaml
# Node name, used to identify the current host machine, will be used in sentry mode
name: jvmm_server

server:
  # Config server type, jvmm is the default type. You can enable multiple options like this: http,sentinel,jvmm
  type: jvmm,http

  # Jvmm server config options
  # The difference between jvmm server and http server is that jvmm server provides the encryption function of communication messages,
  # the client must use a private protocol to communicate with the server.
  # And jvmm server is a tcp long connection, the client and the server can communicate in both directions.
  # --------------------------------------------------------------------------------------------------------------------------------------------------------
  jvmm:
    # Jvmm server running port
    port: 5010
    # Whether to allow adaptive search for available ports, if the port you configured is already occupied, after enabling this option,
    # it will automatically increase the search for available ports, but this operation can be performed up to 5 times.
    adaptivePort: true
    # Communication authentication related configuration
    auth:
      enable: false
      username: 123456
      password: 123456
    maxChunkSize: 52428800

  # Http server config options.
  # The configuration of opening the http segment will start the http server, and you can call the relevant api through http requests.
  # --------------------------------------------------------------------------------------------------------------------------------------------------------
  http:
    # Http server running port
    port: 8080
    # Whether to allow adaptive search for available ports, if the port you configured is already occupied, after enabling this option,
    # it will automatically increase the search for available ports, but this operation can be performed up to 5 times.
    adaptivePort: true
    # Configure Basic authentication for http server
    auth:
      enable: true
      username: 123456
      password: 123456
    maxChunkSize: 52428800
    # Https related configuration
    ssl:
      enable: false
      # Trusted CA certificate, this configuration can be deleted if not specified
      certCa: ./config/cert-ca.pem
      # Configure the certificate file path
      cert: ./config/cert.pem
      # Configure the path to the certificate key file
      certKey: ./config/cert.key
      # Configure the password of the secret key, if there is no password, you can not configure it
      keyPassword: 123456
      # Whether to use openssl implementation, the default is true, if false, the implementation provided by JDK will be used
      openssl: true

  # Sentinel config options.
  # Enabling the sentinel segment configuration will start the Jvmm sentinel mode, and the sentinel will regularly push monitoring data to subscribers
  # --------------------------------------------------------------------------------------------------------------------------------------------------------
  sentinel:
    # Subscriber list, if this item is not configured or the list is empty, the sentry mode cannot be started
    # The subscriber's push interface only supports Basic authentication, which is configured through the auth segment
    - subscribers:
        - url: http://127.0.0.1:9999/monitor/subscriber
          auth:
            enable: true
            username: 123456
            password: 123456
        - url: http://monitor.example.com:9999/monitor/subscriber
      # The interval between sentinels executing tasks, unit is second.
      # Notice! Some collection items take time, it is recommended that the interval be greater than 1 second.
      interval: 15
      # Total sending times, -1 means unlimited
      count: 20
      # The collection items executed by the sentinel, the sentinel will collect the data and send it to subscribers.
      # Optional values: process|disk|disk_io|cpu|network|sys|sys_memory|sys_file|jvm_classloading|jvm_classloader|jvm_compilation|jvm_gc|jvm_memory|jvm_memory_manager|jvm_memory_pool|jvm_thread
      tasks:
        - process
        - disk
        - disk_io
        - cpu
    # You can define multiple sentinels, which perform different tasks
    - subscribers:
        - url: http://monitor.example.com:9999/monitor/subscriber
      interval: 15
      count: -1
      tasks:
        - jvm_gc
        - jvm_memory
        - jvm_memory_pool
        - jvm_memory_manager
        - jvm_thread
        - jvm_classloader
        - jvm_classloading

# The default Jvmm log configuration, if no SLF4J log implementation is found in the startup environment, use this configuration.
log:
  # Log level: error, warn, info, debug, trace
  level: INFO
  # Log file output directory
  file: log
  # Log file prefix
  fileName: jvmm
  # If the current log file size exceeds this value, a new log file will be generated, in MB.
  fileLimitSize: 10
  # Log output formatting matching rules.
  # The color output supports ANSI code, but it will only appear in the standard output of the console, and will not pollute the log file.
  pattern: "%ansi{%date{yyyy-MM-dd HH:mm:ss}}{36} %ansi{%level}{ERROR=31,INFO=32,WARN=33,DEBUG=34,TRACE=35} %ansi{%class}{38;5;14} : %msg"
  # Output type, support standard output and file output.
  printers: std,file

# The number of worker threads for the service
workThread: 2
```

如果你不使用文件的方式配置，也可以在代码中构造Configuration

```java
public class ServerBootDemo {
    public static void main(String[] args) {
        AuthOptionConf globalAuth = new AuthOptionConf()
                .setEnable(true)
                .setUsername("jvmm-acc")
                .setPassword("jvmm-pass");

        JvmmServerConf jvmmServer = new JvmmServerConf()
                .setPort(5010)
                .setAdaptivePort(true)
                .setAuth(globalAuth);

        HttpServerConf httpServer = new HttpServerConf()
                .setPort(8080)
                .setAdaptivePort(true)
                .setAuth(globalAuth);

      SentinelConf sentinel = new SentinelConf()
              .addSubscriber(new SentinelSubscriberConf().setUrl("http://exaple.jvmm.com/subscriber"))
              .addSubscriber(new SentinelSubscriberConf().setUrl("http://127.0.0.1:8080/subscriber")
                      .setAuth(new AuthOptionConf().setEnable(true)
                              .setUsername("auth-account")
                              .setPassword("auth-password")))
              .setInterval(10)
              .setCount(-1);

        Configuration config = new Configuration()
                .setName("jvmm-server")
                .setWorkThread(2)
                .setLog(new LogConf().setLevel(LoggerLevel.INFO))
                .setServer(new ServerConf()
                        .setType("jvmm,sentinel")
                        .setJvmm(jvmmServer)
                        .setHttp(httpServer)
                        .setSentinel(sentinel));

        ServerBootstrap server = ServerBootstrap.getInstance(config);
        server.start(msg -> System.out.println(msg));
    }
}
```

# 三种Service

server提供了三种service：

* **jvmm**：独有的RPC通信模块，安全可靠，需要使用项目中的[客户端连接工具](../client/README.md#连接模式)或`JvmmConnector`连接
* **http**：对外提供http服务，不限编程语言，请求相应的http接口即可
* **sentinel**：在server端启动一个哨兵，定时采集数据并上报给订阅者

它们可以同时运行，启动服务类型在配置文件中的`server.type`配置或代码中构造Configuration时`ServerConf#setType()`配置，如需代码关闭服务可以这样调用

```java
public class ServerStopDemo {
    public static void stop() {
        //  关闭某一个服务
        ServerContext.stop(ServerType.jvmm);

        //  关闭所有服务
        ServerContext.stopAll();
    }
}
```

## 1. Jvmm Service

Jvmm Service启动后会在程序中启动一个Jvmm RPC服务，与之通信需要使用`JvmmConnector`，相关配置段在[config.yml](../server/src/main/resources/config.yml)中的`server.jvmm`。

> Jvmm RPC与Http不同的是Jvmm服务支持TCP长连接，可长期保持双向通信，并且每个消息包都会进行身份校验和数据加密，**客户端工具只能用这种方式连接**。
>
> 请求为JvmmRequest，响应为JvmmResponse

调用代码示例如下：

```java
public class ServerConveyDemo {

    private static Logger logger;

    public static void main(String[] args) throws Exception {
        LoggerInitializer.init(LoggerLevel.INFO);
        logger = LoggerFactory.logger(ServerConveyDemo.class);

        EventLoopGroup executor = ChannelInitializers.newEventLoopGroup(1);

        JvmmRequest request = JvmmRequest.create().setType(GlobalType.JVMM_TYPE_PING);

        //  向jvmm服务器发送一个消息并同步等待
        JvmmResponse response = sendMsgOnce(executor, request);

        // 向jvmm服务器发送一个消息，异步响应
        sendMsgOnceAsync(executor, request, System.out::println);

        //  得到一个保持活跃的连接器
        JvmmConnector connector = getKeepAliveConnector(executor);
        connector.send(request);
    }

    private static JvmmResponse sendMsgOnce(EventLoopGroup executor, JvmmRequest request) throws Exception {
        return JvmmConnector.waitForResponse(executor, "127.0.0.1:5010", request);
    }

    private static void sendMsgOnceAsync(EventLoopGroup executor, JvmmRequest request, JvmmConnector.MsgReceiveListener listener) throws Exception {
        JvmmConnector connector = JvmmConnector.newInstance("127.0.0.1", 5010, executor, false, "jvmm_acc", "jvmm_pwd");
        Future<Boolean> f1 = connector.connect();
        if (f1.await(3, TimeUnit.SECONDS)) {
            if (f1.getNow()) {

                connector.registerCloseListener(() -> {
                    logger.info("Jvmm connector closed");
                    executor.shutdownGracefully();
                });

                connector.registerListener(response -> {
                    listener.onMessage(response);
                    connector.close();
                });

                connector.send(request);
            } else {
                logger.error("Authentication failed!");
            }
        } else {
            logger.error("Connect time out");
        }
    }

    private static JvmmConnector getKeepAliveConnector(EventLoopGroup executor) throws Exception {
        JvmmConnector connector = JvmmConnector.newInstance("127.0.0.1", 5010, executor, true, "jvmm_acc", "jvmm_pwd");
        Future<Boolean> f1 = connector.connect();
        if (f1.await(3, TimeUnit.SECONDS)) {
            if (f1.getNow()) {

                connector.registerCloseListener(() -> {
                    logger.info("Jvmm connector closed");
                    executor.shutdownGracefully();
                });

                return connector;
            } else {
                logger.error("Authentication failed!");
            }
        } else {
            logger.error("Connect time out");
        }
        return null;
    }
}
```

### 调用接口

Jvmm Service提供了以下API接口：

| Type                                      | Data                                                                                            | Description                                                                           |
|-------------------------------------------|-------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| JVMM_TYPE_COLLECT_SYS_INFO                | /                                                                                               | 采集操作系统信息                                                                              |
| JVMM_TYPE_COLLECT_SYS_MEMORY_INFO         | /                                                                                               | 采集操作系统内存数据                                                                            |
| JVMM_TYPE_COLLECT_SYS_FILE_INFO           | /                                                                                               | 采集操作系统磁盘分区使用情况数据                                                                      |
| JVMM_TYPE_COLLECT_PROCESS_INFO            | /                                                                                               | 采集当前进程数据                                                                              |
| JVMM_TYPE_COLLECT_DISK_INFO               | /                                                                                               | 采集物理机磁盘数据                                                                             |
| JVMM_TYPE_COLLECT_DISK_IO_INFO            | /                                                                                               | 采集物理机磁盘IO及吞吐量数据                                                                       |
| JVMM_TYPE_COLLECT_CPU_INFO                | /                                                                                               | 采集物理机CPU负载数据                                                                          |
| JVMM_TYPE_COLLECT_NETWORK_INFO            | /                                                                                               | 采集物理机网卡信息及IO数据                                                                        |
| JVMM_TYPE_COLLECT_JVM_CLASSLOADING_INFO   | /                                                                                               | 采集JVM类加载信息                                                                            |
| JVMM_TYPE_COLLECT_JVM_CLASSLOADER_INFO    | /                                                                                               | 采集JVM类加载器信息                                                                           |
| JVMM_TYPE_COLLECT_JVM_COMPILATION_INFO    | /                                                                                               | 采集JVM编译信息                                                                             |
| JVMM_TYPE_COLLECT_JVM_GC_INFO             | /                                                                                               | 采集JVM垃圾收集器信息                                                                          |
| JVMM_TYPE_COLLECT_JVM_MEMORY_MANAGER_INFO | /                                                                                               | 采集JVM内存管理器信息                                                                          |
| JVMM_TYPE_COLLECT_JVM_MEMORY_POOL_INFO    | /                                                                                               | 采集JVM内存池信息                                                                            |
| JVMM_TYPE_COLLECT_JVM_MEMORY_INFO         | /                                                                                               | 采集JVM内存使用情况                                                                           |
| JVMM_TYPE_COLLECT_JVM_THREAD_INFO         | /                                                                                               | 采集JVM线程统计数据                                                                           |
| JVMM_TYPE_COLLECT_JVM_THREAD_STACK        | 见[ThreadInfoDTO](src/main/java/org/beifengtz/jvmm/server/entity/dto/ThreadInfoDTO.java)         | 采集JVM线程堆栈数据                                                                           |
| JVMM_TYPE_COLLECT_JVM_DUMP_THREAD         | /                                                                                               | dump所有线程堆栈数据                                                                          |
| JVMM_TYPE_COLLECT_BATCH                   | 见[CollectOptions](src/main/java/org/beifengtz/jvmm/server/entity/conf/CollectOptions.java)      | 根据选项批量采集数据                                                                            |
| JVMM_TYPE_EXECUTE_GC                      | /                                                                                               | 执行gc                                                                                  |
| JVMM_TYPE_EXECUTE_JAVA_PROCESS            | /                                                                                               | 列出所有Java进程                                                                            |
| JVMM_TYPE_EXECUTE_JVM_TOOL                | String                                                                                          | 执行jvm tool命令                                                                          |
| JVMM_TYPE_PROFILER_SAMPLE                 | 见[ProfilerSampleDTO](src/main/java/org/beifengtz/jvmm/server/entity/dto/ProfilerSampleDTO.java) | 生成火焰图                                                                                 |
| JVMM_TYPE_PROFILER_EXECUTE                | String                                                                                          | 执行profiler命令，见[async-profiler](https://github.com/jvm-profiling-tools/async-profiler) |
| JVMM_TYPE_SERVER_SHUTDOWN                 | String                                                                                          | 关闭服务，data为服务类型                                                                        |
| JVMM_TYPE_EXECUTE_JAD                     | JsonObject，其属性为：className(String), methodName(String)                                           | 代码反编译（仅支持agent）                                                                       |
| JVMM_TYPE_EXECUTE_LOAD_PATCH              | JsonArray，其元素为[PatchDTO](src/main/java/org/beifengtz/jvmm/server/entity/dto/PatchDTO.java)      | 代码热更，当指定ClassLoader的hash时只针对于改ClassLoader加载的类进行热更                                     |

## 2. Http Service

Http Service启动之后会在程序中启动一个http服务，你可以在浏览器或者以http协议调用相关接口，相关配置段在[config.yml](../server/src/main/resources/config.yml)中的`server.http`。

### Http接口

Http Service提供了以下API接口：

| Uri                         | 方法   | 参数                                    | Body                                                                                            | 描述                                                                                    |
|-----------------------------|------|---------------------------------------|-------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| /collect/process            | GET  | /                                     | /                                                                                               | 采集进程信息                                                                                |
| /collect/disk               | GET  | /                                     | /                                                                                               | 采集物理机磁盘数据                                                                             |
| /collect/disk_io            | GET  | /                                     | /                                                                                               | 采集物理机磁盘IO及吞吐量数据                                                                       |
| /collect/cpu                | GET  | /                                     | /                                                                                               | 采集物理机CPU负载数据                                                                          |
| /collect/network            | GET  | /                                     | /                                                                                               | 采集物理机网卡信息及IO数据                                                                        |
| /collect/sys                | GET  | /                                     | /                                                                                               | 采集操作系统信息                                                                              |
| /collect/sys/memory         | GET  | /                                     | /                                                                                               | 采集操作系统内存数据                                                                            |
| /collect/sys/file           | GET  | /                                     | /                                                                                               | 采集操作系统磁盘分区使用情况数据                                                                      |
| /collect/jvm/classloading   | GET  | /                                     | /                                                                                               | 采集JVM类加载信息                                                                            |
| /collect/jvm/classloader    | GET  | /                                     | /                                                                                               | 采集JVM类加载器信息                                                                           |
| /collect/jvm/compilation    | GET  | /                                     | /                                                                                               | 采集JVM编译信息                                                                             |
| /collect/jvm/gc             | GET  | /                                     | /                                                                                               | 采集JVM垃圾收集器信息                                                                          |
| /collect/jvm/memory_manager | GET  | /                                     | /                                                                                               | 采集JVM内存管理器信息                                                                          |
| /collect/jvm/memory_pool    | GET  | /                                     | /                                                                                               | 采集JVM内存池信息                                                                            |
| /collect/jvm/memory         | GET  | /                                     | /                                                                                               | 采集JVM内存使用情况                                                                           |
| /collect/jvm/thread         | GET  | /                                     | /                                                                                               | 采集JVM线程统计数据                                                                           |
| /collect/jvm/thread_stack   | POST | /                                     | 见[ThreadInfoDTO](src/main/java/org/beifengtz/jvmm/server/entity/dto/ThreadInfoDTO.java)         | 采集JVM线程堆栈数据                                                                           |
| /collect/jvm/dump_thread    | GET  | /                                     | /                                                                                               | dump所有线程堆栈数据                                                                          |
| /collect/by_options         | POST | /                                     | 见[CollectOptions](src/main/java/org/beifengtz/jvmm/server/entity/conf/CollectOptions.java)      | 根据选项批量采集数据                                                                            |
| /execute/gc                 | GET  | /                                     | /                                                                                               | 执行gc                                                                                  |
| /execute/jps                | GET  | /                                     | /                                                                                               | 列出所有Java进程                                                                            |
| /execute/jvm_tool           | POST | /                                     | command(String)                                                                                 | 执行jvm tool命令                                                                          |
| /profiler/flame_graph       | POST | /                                     | 见[ProfilerSampleDTO](src/main/java/org/beifengtz/jvmm/server/entity/dto/ProfilerSampleDTO.java) | 生成火焰图                                                                                 |
| /profiler/execute           | POST | /                                     | command(String)                                                                                 | 执行profiler命令，见[async-profiler](https://github.com/jvm-profiling-tools/async-profiler) |
| /server/shutdown            | GET  | target(String)                        | /                                                                                               | 关闭服务，data为服务类型                                                                        |
| /execute/jad                | GET  | className(String), methodName(String) | /                                                                                               | 代码反编译（仅支持agent）                                                                       |
| /execute/load_patch         | POST | /                                     | JsonArray，其元素为[PatchDTO](src/main/java/org/beifengtz/jvmm/server/entity/dto/PatchDTO.java)      | 代码热更，当指定ClassLoader的hash时只针对于改ClassLoader加载的类进行热更                                     |

### 3. Sentinel Service

哨兵模式的运作逻辑是**定期采集指定数据项然后向订阅者推送**，一般可以用于节点探活、健康监控等场景。

你需要**提前搭建好订阅者服务**并对外公开一个接收哨兵通知数据的http接口，如果接口访问需要进行身份认证，哨兵模式支持 **Basic** 方式认证，将订阅者信息配置后哨兵将定时向此接口推送监控数据，相关配置段在[config.yml](../server/src/main/resources/config.yml)中的`server.sentinel`。

总共支持以下采集项，其中`disk_io`、`cpu`、`network`执行需要耗时，程序内部为异步回调实现，但如果你是Http API请求会表现为同步，每一项等待时间为`1s`。

```json
[
  "process",
  "disk",
  "disk_io",
  "cpu",
  "network",
  "sys",
  "sys_memory",
  "sys_file",
  "jvm_classloading",
  "jvm_classloader",
  "jvm_compilation",
  "jvm_gc",
  "jvm_memory",
  "jvm_memory_manager",
  "jvm_memory_pool",
  "jvm_thread"
]
```