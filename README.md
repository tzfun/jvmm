## Jvmm

![license](https://img.shields.io/badge/license-Apache--2.0-yellow)
[![maven](https://img.shields.io/badge/maven-2.0.0-blue)](https://search.maven.org/search?q=g:io.github.tzfun.jvmm)

Jvmm是一个轻量的JVM监控工具，提供有丰富的监控功能：可动态查看正在运行的Java虚拟机信息（Runtime、内存、CPU、线程、GC等）以及物理机信息（内存、磁盘等），可远程向Java进程甚至物理机上的其他Java进程发起内部调用，可生成火焰图，并提供有三种服务模式：jvmm服务、http服务、哨兵模式，适合用于服务健康监控、线上调优排查问题、性能测试等场景。

## 支持功能

* 支持查询Java运行时启动参数、虚拟机参数
* 支持监控虚拟机gc统计信息、内存使用情况、内存池信息、类文件加载统计、线程堆栈
* 支持监控物理机基础信息、内存使用情况、磁盘使用情况、cpu负载
* 支持远程执行JDK自带工具，jps、jstat、jstack、jinfo、jmap、jcmd等
* 支持生成火焰图
* 提供命令行交互工具，同时支持装载和连接jvmm server功能，引导式命令参数填写，上手非常简单
* 支持三种模式供你应对大部分监控场景，并且可同时开启多个
  * jvmm server：提供接口调用API，长连接，自定义安全rpc，需使用jvmm客户端连接
  * http server：提供Http接口，不限制语言远程调用
  * sentinel：启动哨兵在Java进程中定时采集数据并上报，上报协议为http，订阅者、采集数据项、身份认证均可配置
* 支持在自己项目中直接使用jvmm，引入server模块即可
* 支持基于core模块二次开发
* 支持 JDK 8+
* 支持 Linux/Mac/Windows

## 获取Jvmm客户端

请先前往[release](https://github.com/tzfun/jvmm/releases)下载[最新版的jvmm](https://github.com/tzfun/jvmm/releases/download/2.0.0/jvmm-2.0.0.zip)

## 快速使用

将Jvmm客户端压缩包解压，然后运行

```shell
java -jar jvmm.jar
```

## 使用文档

* [客户端工具文档](client/README.md)
* [Java Agent文档](agent/README.md)
* [Core组件文档](core/README.md)
* [Server组件文档](server/README.md)

## 示例

在这里提供了一些简单示例

* [核心组件API调用示例](demo/src/main/java/org/beifengtz/jvmm/demo/ApiDemo.java)
* [Server组件使用示例](demo/src/main/java/org/beifengtz/jvmm/demo/ServerBootDemo.java)
* [Jvmm连接工具使用示例](demo/src/main/java/org/beifengtz/jvmm/demo/ServerConveyDemo.java)

生成火焰图示例

![profiler.png](doc/profiler.png)

Dashboard应用示例

![dashboard](doc/dashboard.jpg)

## 联系作者

在使用过程中遇到任何问题，或者对本项目有独特的见解或建议，欢迎[提交issue](https://github.com/tzfun/jvmm/issues)

联系邮箱：[beifengtz@qq.com](mailto://beifengtz@qq.com)

## QAS

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
