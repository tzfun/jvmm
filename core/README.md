core模块是jvmm运行的核心模块，如果本项目提供的rpc方式无法满足你的使用场景，或者你想优化server使用自己项目的网络驱动，你可以在此模块基础上进行开发。

## 依赖引入

Maven引入
```xml
<dependency>
  <groupId>io.github.tzfun.jvmm</groupId>
  <artifactId>jvmm-core</artifactId>
  <version>${jvmm-version}</version>
</dependency>
```

或 gradle引入

```groovy
implementation "io.github.tzfun.jvmm:jvmm-core:${jvmm-version}"
```

## 示例代码

采集器调用示例
```java
public class ApiDemo {
    public static void main(String[] args) {
        LoggerInitializer.init(LoggerLevel.INFO);
        //  获取采集器
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
    }
}
```

执行器调用示例
```java
public class ApiDemo {
    public static void main(String[] args) {
        LoggerInitializer.init(LoggerLevel.INFO);
        //  jvmm执行器
        JvmmExecutor executor = JvmmFactory.getExecutor();

        executor.gc();
        executor.setMemoryVerbose(true);
        executor.setClassLoadingVerbose(true);
        executor.setThreadContentionMonitoringEnabled(true);
        executor.setThreadCpuTimeEnabled(true);
        executor.resetPeakThreadCount();
        Pair<List<String>, Boolean> result = executor.executeJvmTools("jstat -gc 1102");
    }
}
```

采样器调用示例（生成火焰图）
```java
public class ApiDemo {
    public static void main(String[] args) {
        LoggerInitializer.init(LoggerLevel.INFO);
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
