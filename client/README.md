## 一、关于Jvmm客户端

使用客户端之前请先下载好程序包，请前往[releases](https://github.com/tzfun/jvmm/releases)下载最新版的jvmm。

### 功能介绍

* 支持向本地任意一个Java进程装载Jvmm，并启动或关闭Jvmm Service
* 支持连接远程Jvmm Server，并向其发起采集数据、gc、生成火焰图、关闭等指令
* 生成[Java Agent](../agent/README.md#premain方式)和[独立启动Server](../server/README.md#独立启动)所需的依赖包

## 二、使用

客户端工具提供有两种方式供你使用

### 引导式执行

引导式填写参数，你只需要**不带任何参数**执行jar包然后根据引导提示操作，第一步是模式选择，进入不同的模式填写的参数也会不一样。

```shell
java -jar jvmm.jar
```

![jvmm attach](../doc/jvmm-attach.gif)

### 命令式执行

命令式执行是在命令行后预填好参数，一次执行。

获取完整的参数信息请执行：

```shell
java -jar jvmm.jar -h
```

例如：

```shell
java -jar jvmm.jar -m attach -c ./config.yml -pid 12345
```

## 三、三种运行模式

PS：**所有模式都可以不带任何参数（引导式）运行，客户端会引导你填写参数信息**，即可以直接执行`java -jar jvmm.jar`。

### 装载模式

装载模式可以向**本地**任意一个正在运行的Java进程装载Jvmm，并根据配置文件配置启动或关闭服务，你不需要重启你的程序。

引导式执行时需**注意第一步模式请选择attach**，而命令式执行需要通过`-m attach`参数指定进入装载模式，其余参数说明如下：

* `-c`：配置文件路径，可以是本地文件路径，也可以是一个http(s)的网络地址，必填
* 指定目标Java进程，以下两个参数二选一即可：
  * `-pid`：目标Java进程的pid，建议使用
  * `-p`：目标Java进程正在监听的 TCP 端口
* `-a`：指定agent jar包，可以是本地文件路径，也可以是一个http(s)的网络地址。选填，默认使用Jvmm客户端生成的jar包
* `-s`：指定server jar包，可以是本地文件路径，也可以是一个http(s)的网络地址。选填，默认使用Jvmm客户端生成的jar包

命令示例
```shell
java -jar jvmm.jar -m attach -c ./config.yml -pid 12345

java -jar jvmm.jar -m attach -c ./config.yml -p 8080

java -jar jvmm.jar -m attach -c http://jvmm.beifengtz.com/config.yml -pid 12345

java -jar jvmm.jar -m attach -c ./config.yml -pid 12345 -a ./jvmm-agent.jar -s ./jvmm-server.jar
```

#### 配置文件

详细配置见[config.yml](../server/src/main/resources/config.yml)

允许重复向同一个进程装载jvmm，其中的service服务会以**差量启停**的方式进行，差量处理的标准是以配置文件中的`server.type`为标准的，以下流程作为示例解释：

> **step 1**：server.type配置为 *jvmm,http,sentinel*，执行后将启动 **jvmm**、**http**、**sentinel** 3 个服务
> 
> **step 2**：server.type配置为 *jvmm,http*，执行后将关闭sentinel服务，jvmm和http服务无影响
> 
> **step 3**：server.type配置为 *none*，执行后将关闭所有服务
> 
> **step 4**：server.type配置为 *jvmm,http*，执行后将启动**jvmm**和**http** 2 个服务

### 连接模式

假设你已经在一个Java进程中启动了jvmm server（配置的`server.type`为**jvmm**），无论这个进程在本地机器上还是远程机器上，你都可以使用客户端连接模式与其建立连接。

引导式执行时请选择client，命令式执行通过`-m client`参数指定进入，其余参数说明如下：

* `-h`：连接地址，IP + 端口，例如`127.0.0.1:5010`
* `-user`：身份认证用户名，选填，如果jvmm server开启了身份认证需填写
* `-pass`：身份认证密码，选填，如果jvmm server开启了身份认证需填写

命令示例
```shell
java -jar jvmm.jar -m client -h 127.0.0.1:5010
```

如果连接成功你将看到如下提示，接下来将进入连接模式与jvmm server进行指令交互
```
[Jvmm] [Info ] Start to connect jvmm agent server...
[Jvmm] [Info ] Connect successful! You can use the 'help' command to learn how to use. Enter 'exit' to safely exit the connection.
>
```

#### 连接模式指令

1. **info** 采集信息指令
   * `-t`: 必填，采集信息类型，允许值：`process`|`disk`|`diskio`|`cpu`|`net`|`sys`|`sysMem`|`sysFile`|`cLoading`|`cLoader`|`comp`|`gc`|`jvmMem`|`memManager`|`memPool`|`thread`|`threadStack`
   * `-f`: 选填，将采集信息结果输出到文件，不填此值将输出到终端

```shell
info -t system

info -t threadStack -f thread_dump.txt
```

2. **profiler** 生成火焰图指令
    * `-c`，采样类型，允许值：samples、total，默认值为samples
    * `-e`，采样事件，允许值：cpu、alloc、lock、wall、itimer，或者是Java方法，格式为ClassName.MethodName，例如：java.lang.Object.toString，默认值为cpu
    * `-f`，采样输出文件，文件后缀名为格式化类型，允许后缀类型：html、txt、jfr，如果不填此参数将默认输出文本内容
    * `-i`，采样单位间隔，单位纳秒，默认值 10000000ns
    * `-t`，采样时间，单位秒，默认值 10s

```shell
profiler

profiler -f profiler.html

profiler -e wall -f profiler.html -t 120
```

![profiler](../doc/profiler.png)

3. **gc** 执行gc，无参数
4. **jps** 列出Java进程，无参数
5. **jtool** 执行Java tools工具命令，前提是你的环境中可以执行：jps、jstack、jstat、jinfo、jmap、jcmd

```shell
jtool jstat -gc 15243
```

6. **shutdown** 关闭正在运行的目标服务
   * `-t`：必填，服务类型，可选值：jvmm、http、sentinel

7. **jad** 代码反编译（仅支持以agent方式载入jvmm的时候使用）
    * `-c`：必填，需要反编译的类路径，例如java.lang.Object
    * `-m`：指定只获取类中的方法，如果不填默认输出整个类源码
    * `-f`：将源码输出到文件，如果不填默认输出在控制台

```shell
jad -c java.lang.Object

jad -c java.lang.Object -m toString

jad -c java.lang.Object -f Object.java
```

### 依赖生成模式

引导式执行时请选择jar，命令式执行通过`-m jar`参数指定进入，无其他参数。生成过程中可能比较耗时，请耐心等待，如果执行成功你将看到如下类似内容：

```text
E:\Jvmm> java -jar jvmm.jar

[1] client,     Connect to remote jvmm server.
[2] attach,     Attach Jvmm to the local java process.
[3] jar,        Generate the jar files required by the java agent.

Select an execution mode(serial number): 3
[Jvmm] [Info ] Starting to generate server jar...
[Jvmm] [Info ] Generated server jar to E:\Jvmm\jvmm-server.jar
[Jvmm] [Info ] Starting to generate agent jar...
[Jvmm] [Info ] Generated agent jar to E:\Jvmm\jvmm-agent.jar
[Jvmm] [Info ] Generate jar finished.
```

执行完之后会生成两个jar包：

* **jvmm-agent.jar**:：以`premain agent`方式启动Jvmm必须的jar包，请前往[premain agent使用Jvmm](../agent/README.md#premain方式)查看
* **jvmm-server.jar**：可独立运行的jar包，使用方法请前往[独立启动Server](../server/README.md#独立启动)