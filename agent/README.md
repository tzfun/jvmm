Jvmm载入宿主程序是以Java Agent方式载入的，提供有`premain`和`agentmain`两种agent方式载入

## premain方式

首先你需要准备好三个文件：

* jvmm-agent.jar
* jvmm-server.jar
* config.yml

前两个文件可以使用Jvmm客户端工具生成，具体操作请移步[Jvmm客户端Jar模式生成Agent Jar](../client/README.md#生成Agent依赖Jar)

配置文件见[config.yml](../server/src/main/resources/config.yml)

在启动你的Java程序时添加如下格式的JVM参数

```shell
java -javaagent:[jvmm-agent.jar路径]=[jvmm-server.jar路径];config=[config.yml路径] -jar xxx.jar
```

PS：上面三个文件路径均支持本地文件路径和http(s)两种格式，给出一个示例：
```shell
java -javaagent:/jvmm-dev/jvmm-agent.jar=/jvmm-dev/jvmm-server.jar;config=http://jvmm.beifengtz.com/config.yml -jar xxx.jar
```

## agentmain方式

如果你需要以agentmain动态载入进程，建议使用本项目提供的客户端工具，具体使用方法见[Jvmm客户端attach模式](../client/README.md#装载)
