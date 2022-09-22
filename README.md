## Jvmm

![license](https://img.shields.io/badge/license-Apache--2.0-yellow)
[![maven](https://img.shields.io/badge/maven-2.0.0-blue)](https://search.maven.org/search?q=g:io.github.tzfun.jvmm)

Jvmm是一个轻量的JVM监控工具，提供有丰富的监控功能：可查看Java虚拟机信息（Runtime、内存、CPU、线程、GC等）以及OS信息（内存、磁盘等），可生成火焰图，提供http、哨兵等三种service模式。适合用于服务健康监控、线上调优、排查问题、性能测试等场景。

## 功能支持

* 支持监控虚拟机gc统计信息、内存使用情况、内存池信息、类文件加载统计、线程堆栈
* 支持监控物理机基础信息、内存使用情况、磁盘使用情况、cpu负载
* 支持获取Java运行时启动参数、虚拟机参数、properties参数
* 支持远程执行JDK自带工具，jps、jstat、jstack、jinfo、jmap、jcmd等
* 支持生成火焰图（采样事件包括CPU、内存分配、线程栈、Java方法调用栈等）
* 支持远程执行GC
* 提供客户端交互工具，支持跨进程attach和远程连接功能
* 支持三种服务模式，足以应对大部分监控场景，可同时开启多个服务
  * jvmm服务：独有通信协议，需使用jvmm客户端远程连接调用
  * http服务：提供Http接口，不限语言远程调用
  * 哨兵：定时采集数据并上报给订阅者
* 支持在自己项目中直接使用jvmm，只需引入server模块
* 支持基于core模块进行二次开发
* 支持 JDK 8+
* 支持 Linux/Mac/Windows

## 获取Jvmm

请前往[release](https://github.com/tzfun/jvmm/releases)下载[最新版的jvmm](https://github.com/tzfun/jvmm/releases/download/2.0.0/jvmm-2.0.0.zip)

## 快速使用

将Jvmm压缩包解压，然后运行

```shell
java -jar jvmm.jar
```

或直接在你的项目中使用

```xml
<dependency>
    <groupId>io.github.tzfun.jvmm</groupId>
    <artifactId>jvmm-server</artifactId>
    <version>${jvmm-version}</version>
</dependency>
```

```java
public class JvmmServerBootDemo {
    public static void main(String[] args) {
        ServerBootstrap server = ServerBootstrap.getInstance();
        server.start(msg -> System.out.println(msg));
    }
}
```

## 使用文档

* [Jvmm工具使用文档](client/README.md)
* [两种Java Agent方式使用Jvmm](agent/README.md)
* [Core模块提供的API接口](core/README.md)
* [Server组件使用及三种服务模式说明文档](server/README.md)

## 示例

在这里提供了一些简单示例

* [API调用示例](demo/src/main/java/org/beifengtz/jvmm/demo/ApiDemo.java)
* [Server启动使用示例](demo/src/main/java/org/beifengtz/jvmm/demo/ServerBootDemo.java)
* [Jvmm连接工具使用示例](demo/src/main/java/org/beifengtz/jvmm/demo/ServerConveyDemo.java)

生成火焰图示例

![profiler.png](doc/profiler.png)

Dashboard应用示例

![dashboard](doc/dashboard.jpg)

## 联系作者

在使用过程中遇到任何问题，或者对本项目有独特的见解或建议，欢迎[提交issue](https://github.com/tzfun/jvmm/issues)

联系邮箱：[beifengtz@qq.com](mailto://beifengtz@qq.com)

## 问题解决

### 1.kernel.perf_event_paranoid权限开关
如果你在生成火焰图时提示`No access to perf events. Try --fdtransfer or --all-user option or 'sysctl kernel.perf_event_paranoid=1'`，原因是系统内核默认禁止了检测系统性能，你需要开启这个选项。

```shell
sudo systcl -w kernel.perf_event_paranoid=1
```

或者修改sysctl文件

```shell
sudo sh -c 'echo "kernel.perf_event_paranoid=1" >> /etc/sysctl.conf'
sudo sysctl -p
```

## 感谢

* profiler支持：https://github.com/jvm-profiling-tools/async-profiler
* 灵感来源以及借鉴参考：https://github.com/alibaba/arthas
