中文版 | [English](README.md)

# Jvmm

Jvmm是一个轻量且安全的Java虚拟机监控器。你可以使用它在运行时（Runtime）进行：JVM监控(内存、CPU负载、GC、Thread Info)、操作系统监控(负载、内存使用率、基础信息等)、执行任务、生成火焰图等，比较适合用于服务状态监控、调试、性能测试。

# 快速使用
tag页下载后解压，执行
```shell
//  向运行在8080端口的进程attach
./attach -p 8080
```

在目标进程中日志会提示运行端口，默认是5010

连接Jvmm,然后获取系统和线程信息
```shell
java -jar jvmm-client.jar -h 127.0.0.1:5010
[Jvmm] [Info ] Start to connect jvmm agent server...
[Jvmm] [Info ] Connect successful! You can use the 'help' command to learn how to use. Enter 'exit' to safely exit the connection.
> info -t systemDynamic
{
   "committedVirtualMemorySize":739393536,
   "freePhysicalMemorySize":4804407296,
   "freeSwapSpaceSize":3897561088,
   "processCpuLoad":2.5464865506569934E-4,
   "processCpuTime":8531250000,
   "systemCpuLoad":0.09008395604971231,
   "totalPhysicalMemorySize":0,
   "totalSwapSpaceSize":27989778432
}
> info -t thread
{
   "peakThreadCount":39,
   "daemonThreadCount":25,
   "threadCount":34,
   "totalStartedThreadCount":52
}
> exit
[Jvmm] [Info ] bye bye...
```

# 详细使用

本项目提供了 `java agent`、`API`、`Server服务` 三种方式可供选择，并且制作了`client命令行工具`与server进行通信，*在未来的版本将支持 web client。*

## client命令行工具

client命令行工具有两个模式：Attach 和 Client，使用它必须先选择一个模式进入，你可以这样查看帮助
```shell
java -jar jvmm-client.jar -help
```

你将会看到：
```
Command usage-
Below will list all of parameters. You need choose running mode firstly.

 -help       Help information.
 -m <mode>   Choose action mode: 'client' or 'attach', default value is client

Attach mode-
Attach jvmm server to another java program in this computer.

 -a <agentJarFile>    The path of the 'jvmm-agent.jar' file. Support relative path, absolute path and network address.
 -c <config>          Agent startup configuration parameters, if not filled in, the default configuration will be used.
 -p <port>            Target java program listening port. If pid is not filled in, this parameter is required.
 -pid <pid>           The pid of target java program. If port is not filled in, this parameter is required.
 -s <serverJarFile>   The path of the 'jvmm-server.jar' file. Support relative path, absolute path and network address.

Client mode-
Connect to jvmm server and execute some commands.

 -h <address>       The address that will connect to the Jvmm server, like '127.0.0.1:5010'.
 -pass <password>   Jvmm server authentication password. If the target jvmm server is auth enable.
 -user <username>   Jvmm server authentication account. If the target jvmm server is auth enable.
```

**Client模式命令**

进入Client模式且与jvmm server连接成功后，你将可以使用以下命令（键入help查看）：
```
You can use the following command in client mode.

profiler: -
Get server sampling report. Only supported on MacOS and Linux.
 -c <counter>    Sample counter type, optional values: samples, total. Default value: samples.
 -e <event>      Sample event, optional values: cpu, alloc, lock, wall, itimer. Default value: cpu.
 -f <file>       Output file path (required *), file type indicates format type.
 -i <interval>   The time interval of the unit to collect samples, the unit is nanosecond. Default value: 10000000 ns.
 -t <time>       Sampling interval time, the unit is second. Default value: 10 s.

gc: -
Execute gc, no arguments.

jps: -
View all java processes running on this physical machine.

shutdown: -
Shutdown jvmm server, no arguments.

info: -
Get information about the target server
 -f <output>   File path (optional), output info to file.
 -t <type>     Info type (required *), optional values: system, systemDynamic, classloading, compilation, gc, process, memory,
               memoryManager, memoryPool, thread, threadStack.
```

## 使用agent

Jvmm提供了 java agent 使用，你可以在被监控程序（这里称之为宿主程序）启动时加入参数载入Jvmm，比如：`-agentpath:/path/jvmm-agent.jar=/path/jvmm-server.jar;config=config.yml`。

如果宿主程序已经启动，并且不太方便重启时，你也可以使用本项目提供的Client工具动态地将 agent 和 server 载入到宿主程序。client工具不仅支持载入功能，还支持与jvmm server通信，不过 client 只是提供一些简单的命令，更多更全的功能需调用API。

你可以前往 **标签** 页下载打包好的程序，解压后将会看到这些文件：

```
|-- jvmm-agent.jar      //  agent包，负责将server载入到宿主程序虚拟机
    jvmm-server.jar     //  server包，对外提供接口服务
    jvmm-client.jar     //  client包，命令行工具，负责将agent和server载入宿主程序
    attach              //  unix环境的attach工具，封装自client命令行
    attach.bat          //  windows环境的attach工具，封装自client命令行
    config.yml          //  server配置文件
```

### 运行时attach agent

直接执行client工具包，以下几个方法任选一个即可
```
//  向运行在8080端口的程序载入agent
java -jar jvmm-client.jar -m attach -a ./jvmm-agent.jar -s ./jvmm-server.jar -c config=./config.yml -p 8080

//  向进程号pid为15000的程序载入agent
java -jar jvmm-client.jar -m attach -a ./jvmm-agent.jar -s ./jvmm-server.jar -c config=./config.yml -pid 15000

//  不引用config.yml单独定义配置，未填配置都将使用默认值
java -jar jvmm-client.jar -m attach -a ./jvmm-agent.jar -s ./jvmm-server.jar -c name=jvmm_test;port.bind=9000;port.autoIncrease=false -pid 15000
```

如果你觉得命令行太长，也可以使用attach可执行程序来达到相同的效果，你只需要修改同级目录下的config.yml以及传入宿主程序运行端口或进程号pid。

Linux 或 Mac 中执行
```
./attach -p 8080

./attach -pid 15000
```

windows中执行
```
attach.bat -p 8080

attach.bat -pid 15000
```

### 启动时attach agent

启动时添加启动参数即可，例如：
```
//  引用配置文件方式
java -agentpath:/path/jvmm-agent.jar=/path/jvmm-server.jar;config=/path/config.yml ...

//  参数配置方式
java -agentpath:/path/jvmm-agent.jar=/path/jvmm-server.jar;name=jvmm_test;port.bind=9000;port.autoIncrease=false ...
```

### 配置

配置文件支持 Yaml 和 Properties文件格式，当然也支持参数配置（方法见上面示例），以下是全部配置项及默认配置值：

yml文件
```yaml
name: jvmm_server
port:
  bind: 5010
  autoIncrease: true

http:
  maxChunkSize: 52428800

security:
  enable: false
  account: 
  password: 

log:
  level: info
  useJvmm: false

workThread: 1
```

properties文件
```properties
name=jvmm_server

port.bind=5010
port.autoIncrease=true

http.maxChunkSize=52428800

security.enable=false
security.account=
security.password=

log.level=info
log.useJvmm=false

workThread=1
```

## API调用

如果你想在自己的程序中调用接口，Jvmm也提供了相应的方案。当前最新版本是 `1.2.1`

maven引入
```xml
<dependency>
  <groupId>io.github.tzfun.jvmm</groupId>
  <artifactId>jvmm-core</artifactId>
  <version>${jvmmVersion}</version>
</dependency>
```

或

gradle引入
```gradle
implementation "io.github.tzfun.jvmm:jvmm-core:${jvmmVersion}"
```

**快速上手**

```java
import org.beifengtz.jvmm.core.JvmmCollector;
import org.beifengtz.jvmm.core.JvmmExecutor;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.JvmmProfiler;
import org.beifengtz.jvmm.core.entity.mx.ClassLoadingInfo;
import org.beifengtz.jvmm.core.entity.mx.CompilationInfo;
import org.beifengtz.jvmm.core.entity.mx.GarbageCollectorInfo;
import org.beifengtz.jvmm.core.entity.mx.MemoryInfo;
import org.beifengtz.jvmm.core.entity.mx.MemoryManagerInfo;
import org.beifengtz.jvmm.core.entity.mx.MemoryPoolInfo;
import org.beifengtz.jvmm.core.entity.mx.ProcessInfo;
import org.beifengtz.jvmm.core.entity.mx.SystemDynamicInfo;
import org.beifengtz.jvmm.core.entity.mx.SystemStaticInfo;
import org.beifengtz.jvmm.core.entity.mx.ThreadDynamicInfo;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerCounter;
import org.beifengtz.jvmm.core.entity.profiler.ProfilerEvent;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ApiCallDemo {
    public static void main(String[] args) {
        //  jvmm收集器，可以获取以下信息：
        //  操作系统：基础信息、Memory、CPU、Process信息
        //  Java虚拟机：Memory、GC、Class、Thread、Compilation信息
        JvmmCollector collector = JvmmFactory.getCollector();

        MemoryInfo memory = collector.getMemory();
        List<MemoryManagerInfo> memoryManager = collector.getMemoryManager();
        List<MemoryPoolInfo> memoryPool = collector.getMemoryPool();
        SystemStaticInfo systemStatic = collector.getSystemStatic();
        SystemDynamicInfo systemDynamic = collector.getSystemDynamic();
        ClassLoadingInfo classLoading = collector.getClassLoading();
        List<GarbageCollectorInfo> garbageCollector = collector.getGarbageCollector();
        CompilationInfo compilation = collector.getCompilation();
        ProcessInfo process = collector.getProcess();
        ThreadDynamicInfo threadDynamic = collector.getThreadDynamic();
        String[] threadsInfo = collector.dumpAllThreads();

        //  jvmm执行器
        JvmmExecutor executor = JvmmFactory.getExecutor();

        executor.gc();
        executor.setMemoryVerbose(true);
        executor.setClassLoadingVerbose(true);
        executor.setThreadContentionMonitoringEnabled(true);
        executor.setThreadCpuTimeEnabled(true);
        executor.resetPeakThreadCount();

        //  jvmm采样器，仅支持 MacOS 和 Linux环境
        JvmmProfiler profiler = JvmmFactory.getProfiler();
        File file = new File("jvmm_test.html");
        //  采集cpu信息，持续时间10秒，输出html报告
        Future<String> future = JvmmFactory.getProfiler().sample(file, ProfilerEvent.cpu, ProfilerCounter.samples, 10, TimeUnit.SECONDS);

        try {
            //  等待时间建议长于采样时间
            future.get(12, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
```

## Server服务

如果你想在你的项目中以服务的方式启动，可以使用本项目提供的 server 模块，一些读取信息接口在server中支持循环读取。

为了访问安全着想，Jvmm构造了一个独有的通信协议，即使在不开启身份认证的情况下，不使用 *JvmmConnector* 无法与 server 通信，当然在实际应用时更建议你开启身份认证。

maven引入
```xml
<dependency>
  <groupId>io.github.tzfun.jvmm</groupId>
  <artifactId>jvmm-server</artifactId>
  <version>${jvmmVersion}</version>
</dependency>
```

或

gradle引入
```gradle
implementation "io.github.tzfun.jvmm:jvmm-server:${jvmmVersion}"
```

**快速上手**

启动 server 样例代码：

```java
import org.beifengtz.jvmm.core.conf.Configuration;
import org.beifengtz.jvmm.server.ServerBootstrap;

public class ServerBootDemo {
    public static void main(String[] args) throws Throwable {
        Configuration config = Configuration.newBuilder()
                .setName("jvmm_server_test")
                .setPort(5010)
                .setAutoIncrease(true)
                .setHttpMaxChunkSize(52428800)
                .setLogLevel("info")
                .setLogUseJvmm(true)
                .setSecurityEnable(true)
                .setSecurityAccount("jvmm_acc")
                .setSecurityPassword("jvmm_pwd")
                .setWorkThread(2)
                .build();
        ServerBootstrap server = ServerBootstrap.getInstance(config);
        server.start();

        Thread.sleep(3000);

        server.stop();
    }
}
```

与server通信样例代码：
```java
package org.beifengtz.jvmm.demo;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import org.beifengtz.jvmm.convey.GlobalType;
import org.beifengtz.jvmm.convey.channel.JvmmChannelInitializer;
import org.beifengtz.jvmm.convey.entity.JvmmRequest;
import org.beifengtz.jvmm.convey.socket.JvmmConnector;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ServerConveyDemo {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup executor = JvmmChannelInitializer.newEventLoopGroup(1);
        JvmmConnector connector = JvmmConnector.newInstance("127.0.0.1", 5010, executor, false, "jvmm_acc", "jvmm_pwd");
        Future<Boolean> f1 = connector.connect();
        if (f1.await(3, TimeUnit.SECONDS)) {
            if (f1.getNow()) {

                connector.registerCloseListener(() -> {
                    System.out.println("Jvmm connector closed");
                    executor.shutdownGracefully();
                });

                connector.registerListener(response -> {
                    if (Objects.equals(response.getType(), GlobalType.JVMM_TYPE_PONG.name())) {
                        System.out.println("pong");
                        connector.close();
                    }
                });

                connector.send(JvmmRequest.create().setType(GlobalType.JVMM_TYPE_PING));
            } else {
                System.err.println("Authentication failed!");
            }
        } else {
            System.err.println("Connect time out");
        }
    }
}
```

# 感谢

* profiler支持：https://github.com/jvm-profiling-tools/async-profiler
* 灵感来源以及借鉴参考：https://github.com/alibaba/arthas
