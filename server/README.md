server模块是基于[core模块](../core/README.md)开发的对外提供API服务的程序，如果你不想通过[Java Agent方式](../agent/README.md)启动Jvmm服务，可以直接在你的项目中使用它。

## 独立启动

server jar包支持直接运行启动，你需要先生成`jvmm-server.jar`包，具体生成方法请前往[Jvmm客户端Jar模式生成Agent Jar](../client/README.md#生成依赖Jar)

生成的`jvmm-server.jar`就是我们独立启动的目标jar包，它可以直接运行：

```shell
java -jar jvmm-server.jar ./config.yml
```

运行时可以指定一个[配置文件](#配置)，如果你不指定将会按一下顺序寻找配置：

1. jvmm-server.jar包同级目录下的`config.yml`文件
2. 如果没找到则使用默认配置

## 项目中引入依赖启动

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

## 示例代码

```java
public class ServerBootDemo {
    public static void main(String[] args) {
        ServerBootstrap.getInstance(Configuration.parseFromUrl("config/config.yml")).start();
    }
}
```

## 配置

你可以像示例代码中那样通过配置文件的方式进行配置，全部配置文件内容如下：

```yaml
# Node name, used to identify the current host machine, will be used in sentry mode
name: jvmm_server

server:
  # Config server type, jvmm is the default type. You can enable multiple options like this: http,sentinel,jvmm
  type: jvmm

  # Jvmm server config options
  # The difference between jvmm server and http server is that jvmm server provides the encryption function of communication messages,
  # the client must use a private protocol to communicate with the server.
  # And jvmm server is a tcp long connection, the client and the server can communicate in both directions.
  # --------------------------------------------------------------------------------------------------------------------------------------------------
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
  # --------------------------------------------------------------------------------------------------------------------------------------------------
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
  # --------------------------------------------------------------------------------------------------------------------------------------------------
  sentinel:
    # Subscriber list, if this item is not configured or the list is empty, the sentry mode cannot be started
    # The subscriber's push interface only supports Basic authentication, which is configured through the auth segment
    subscribers:
      - url: http://127.0.0.1:9999/monitor/subscriber
        auth:
          enable: true
          username: 123456
          password: 123456
      - url: http://monitor.example.com:9999/monitor/subscriber
    # The interval between sentinels executing tasks, unit is second
    interval: 15
    # When static options are not enabled, the first N pushes after Sentinel is started will be filled with static data.
    # This value N is configured sendStaticInfoTimes
    sendStaticInfoTimes: 10
    # Sentinel content options for each push
    options:
      classloading: false
      compilation: false
      gc: false
      memory: false
      memoryManager: false
      memoryPool: false
      systemDynamic: false
      threadDynamic: false
      # The next information is static and will not change over time.
      # It is generally not recommended that you enable these options, because it is repetitive and unnecessary.
      # It is recommended to modify sendStaticInfoTimes configuration instead.
      system: false
      process: false

# Log level configuration
log:
  # Log level: error, warn, info, debug, trace, off
  level: info
  #  Whether to use jvmm log, if it is false, it will try to use the log of the host program according to the host program environment.
  #  But this does not guarantee that the log provider of the host program can be successfully searched
  useJvmm: true

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
                .addSubscribers(new SubscriberConf().setUrl("http://exaple.jvmm.com/subscriber"))
                .addSubscribers(new SubscriberConf().setUrl("http://127.0.0.1:8080/subscriber")
                        .setAuth(new AuthOptionConf().setEnable(true)
                                .setUsername("auth-account")
                                .setPassword("auth-password")))
                .setInterval(10)
                .setSendStaticInfoTimes(5);

        Configuration config = new Configuration()
                .setName("jvmm-server")
                .setWorkThread(2)
                .setLog(new LogConf().setLevel("info").setUseJvmm(true))
                .setServer(new ServerConf().setType("jvmm,sentinel")
                        .setJvmm(jvmmServer)
                        .setHttp(httpServer)
                        .setSentinel(sentinel));

        ServerBootstrap server = ServerBootstrap.getInstance(config);
        server.start(msg -> System.out.println(msg));
    }
}
```

## 三种Service

三种service可以同时运行，如需关闭服务可以这样调用

```java
public class ServerStopDemo {
    public static void stop() {
        //  关闭某一个服务
        ServerContext.stop(ServerType.jvmm);

        //  关闭所有服务
        ServerBootstrap.getInstance().stop();
    }
}
```

### 1. jvmm service

jvmm service启动后会在程序中启动一个jvmm rpc服务，与之通信需要使用`JvmmConnector`，相关配置段在config.yml中的`server.jvmm`。

> jvmm rpc与http不同的是jvmm服务支持tcp长连接，可长期保持双向通信，并且每个消息包都会进行身份校验，客户端工具只能用这种方式连接。
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

#### 调用接口

| Type                                     | data                                                                                            | 描述                                                                                    |
|------------------------------------------|-------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| JVMM_TYPE_COLLECT_SYSTEM_STATIC_INFO     | /                                                                                               | 采集系统静态数据                                                                              |
| JVMM_TYPE_COLLECT_CLASSLOADING_INFO      | /                                                                                               | 采集JVM类加载信息                                                                            |
| JVMM_TYPE_COLLECT_COMPILATION_INFO       | /                                                                                               | 采集JVM编译信息                                                                             |
| JVMM_TYPE_COLLECT_PROCESS_INFO           | /                                                                                               | 采集进程信息                                                                                |
| JVMM_TYPE_COLLECT_GARBAGE_COLLECTOR_INFO | /                                                                                               | 采集JVM垃圾收集器信息                                                                          |
| JVMM_TYPE_COLLECT_MEMORY_MANAGER_INFO    | /                                                                                               | 采集JVM内存管理器信息                                                                          |
| JVMM_TYPE_COLLECT_MEMORY_POOL_INFO       | /                                                                                               | 采集JVM内存池信息                                                                            |
| JVMM_TYPE_COLLECT_MEMORY_INFO            | /                                                                                               | 采集JVM内存使用情况                                                                           |
| JVMM_TYPE_COLLECT_SYSTEM_DYNAMIC_INFO    | /                                                                                               | 采集系统动态数据，包含cpu负载、内存、磁盘使用情况                                                            |
| JVMM_TYPE_COLLECT_THREAD_DYNAMIC_INFO    | /                                                                                               | 采集JVM线程统计数据                                                                           |
| JVMM_TYPE_COLLECT_THREAD_INFO            | 见[ThreadInfoDTO](src/main/java/org/beifengtz/jvmm/server/entity/dto/ThreadInfoDTO.java)         | 采集JVM线程堆栈数据                                                                           |
| JVMM_TYPE_DUMP_THREAD_INFO               | /                                                                                               | dump所有线程堆栈数据                                                                          |
| JVMM_TYPE_COLLECT_BATCH                  | 见[CollectOptions](src/main/java/org/beifengtz/jvmm/server/entity/conf/CollectOptions.java)      | 根据选项批量采集数据                                                                            |
| JVMM_TYPE_EXECUTE_GC                     | /                                                                                               | 执行gc                                                                                  |
| JVMM_TYPE_EXECUTE_JAVA_PROCESS           | /                                                                                               | 列出所有Java进程                                                                            |
| JVMM_TYPE_EXECUTE_JVM_TOOL               | String                                                                                          | 执行jvm tool命令                                                                          |
| JVMM_TYPE_PROFILER_SAMPLE                | 见[ProfilerSampleDTO](src/main/java/org/beifengtz/jvmm/server/entity/dto/ProfilerSampleDTO.java) | 生成火焰图                                                                                 |
| JVMM_TYPE_PROFILER_EXECUTE               | String                                                                                          | 执行profiler命令，见[async-profiler](https://github.com/jvm-profiling-tools/async-profiler) |
| JVMM_TYPE_SERVER_SHUTDOWN                | String                                                                                          | 关闭服务，data为服务类型                                                                        |
| JVMM_TYPE_EXECUTE_JAD                    | JsonObject，其属性为：className(String), methodName(String)                                           | 代码反编译（仅支持agent）                                                                       |
| JVMM_TYPE_EXECUTE_LOAD_PATCH             | JsonArray，其元素为[PatchDTO](src/main/java/org/beifengtz/jvmm/server/entity/dto/PatchDTO.java)      | 代码热更，当指定ClassLoader的hash时只针对于改ClassLoader加载的类进行热更                                     |
### 2. http service

http service启动之后会在程序中启动一个http服务，你可以在浏览器或者以http协议调用相关接口，相关配置段在config.yml中的`server.http`。

#### Http接口

| uri                     | 方法   | 参数                                    | body                                                                                            | 描述                                                                                    |
|-------------------------|------|---------------------------------------|-------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| /collect/system_static  | GET  | /                                     | /                                                                                               | 采集系统静态数据                                                                              |
| /collect/classloading   | GET  | /                                     | /                                                                                               | 采集JVM类加载信息                                                                            |
| /collect/compilation    | GET  | /                                     | /                                                                                               | 采集JVM编译信息                                                                             |
| /collect/process        | GET  | /                                     | /                                                                                               | 采集进程信息                                                                                |
| /collect/gc             | GET  | /                                     | /                                                                                               | 采集JVM垃圾收集器信息                                                                          |
| /collect/memory_manager | GET  | /                                     | /                                                                                               | 采集JVM内存管理器信息                                                                          |
| /collect/memory_pool    | GET  | /                                     | /                                                                                               | 采集JVM内存池信息                                                                            |
| /collect/memory         | GET  | /                                     | /                                                                                               | 采集JVM内存使用情况                                                                           |
| /collect/system_dynamic | GET  | /                                     | /                                                                                               | 采集系统动态数据，包含cpu负载、内存、磁盘使用情况                                                            |
| /collect/thread_dynamic | GET  | /                                     | /                                                                                               | 采集JVM线程统计数据                                                                           |
| /collect/thread         | POST | /                                     | 见[ThreadInfoDTO](src/main/java/org/beifengtz/jvmm/server/entity/dto/ThreadInfoDTO.java)         | 采集JVM线程堆栈数据                                                                           |
| /collect/dump_thread    | GET  | /                                     | /                                                                                               | dump所有线程堆栈数据                                                                          |
| /collect/by_options     | POST | /                                     | 见[CollectOptions](src/main/java/org/beifengtz/jvmm/server/entity/conf/CollectOptions.java)      | 根据选项批量采集数据                                                                            |
| /execute/gc             | GET  | /                                     | /                                                                                               | 执行gc                                                                                  |
| /execute/jps            | GET  | /                                     | /                                                                                               | 列出所有Java进程                                                                            |
| /execute/jvm_tool       | POST | /                                     | command(String)                                                                                 | 执行jvm tool命令                                                                          |
| /profiler/flame_graph   | POST | /                                     | 见[ProfilerSampleDTO](src/main/java/org/beifengtz/jvmm/server/entity/dto/ProfilerSampleDTO.java) | 生成火焰图                                                                                 |
| /profiler/execute       | POST | /                                     | command(String)                                                                                 | 执行profiler命令，见[async-profiler](https://github.com/jvm-profiling-tools/async-profiler) |
| /server/shutdown        | GET  | target(String)                        | /                                                                                               | 关闭服务，data为服务类型                                                                        |
| /execute/jad            | GET  | className(String), methodName(String) | /                                                                                               | 代码反编译（仅支持agent）                                                                       |
| /execute/load_patch     | POST | /                                     | JsonArray，其元素为[PatchDTO](src/main/java/org/beifengtz/jvmm/server/entity/dto/PatchDTO.java)      | 代码热更，当指定ClassLoader的hash时只针对于改ClassLoader加载的类进行热更                                     |

### 3. sentinel service

哨兵模式的运作逻辑是**定期采集指定数据项然后向订阅者推送**，一般可以用于节点探活、健康监控等场景。

你需要提前搭建好订阅者服务并对外公开一个http接口，如果接口访问需要进行身份认证，哨兵模式支持 Basic 方式认证，将订阅者信息配置后哨兵将定时向此接口推送监控数据，相关配置段在config.yml中的`server.sentinel`。

> PS：`sendStaticInfoTimes`配置项是指哨兵启动之后前多少次会推送静态数据，一般一个进程启动后的物理环境信息、进程信息等是不变的，这些信息称之为静态数据，为了避免每次推送体积较大且重复的数据，仅启动前N次进行推送，如果你仍然希望每次推送这些静态数据可以在`options`中打开。
