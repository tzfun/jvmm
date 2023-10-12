# jvmm-aop

jvmm aop是一个的面向切面编程工具，它基于 ASM 实现，上手使用简单，开箱即用。

# 使用

## 依赖引入

maven或gradle引入
```xml
<dependency>
    <groupId>io.github.tzfun.jvmm</groupId>
    <artifactId>jvmm-aop</artifactId>
    <version>${jvmm-version}</version>
</dependency>
```

## 方式一：注解

使用注解的方式很简单，只需两步： 
1. 定义一个注解有 AspectJoin 的 MethodListener 子类。
2. 使用AspectInitializer 初始化

对所有 util 包下的方法切入
```java
import org.beifengtz.jvmm.aop.core.MethodListener;
import org.beifengtz.jvmm.aop.core.MethodInfo;

@AspectJoin(
        classPattern = "org.beifengtz.jvmm.*.util.*"
)
public class AopHandler extends MethodListener {
    // 方法执行前触发
    @Override
    public void before(MethodInfo info) throws Throwable {
        
    }

    //  方法正常返回前触发
    @Override
    public void afterReturning(MethodInfo info, Object returnValue) throws Throwable {
        
    }

    //  方法执行抛出异常前触发
    @Override
    public void afterThrowing(MethodInfo info, Throwable throwable) throws Throwable {
        
    }

    //  无论方法正常返回还是抛出异常，都会触发
    @Override
    public void after(MethodInfo info, Object returnVal, Throwable throwable) throws Throwable {
        
    }
}
```

初始化并扫描 AspectJoin 注解
```java
import org.beifengtz.jvmm.aop.AspectInitializer;
import java.lang.instrument.Instrumentation;

public class TestAgent {
    public static void premain(Instrumentation instrumentation) {
        AspectInitializer.init("org.beifengtz", instrumentation);
    }
}
```

> PS: 内部提供了两个 Listener 可以直接开箱使用
> 1. org.beifengtz.jvmm.aop.listener.MethodExecuteTimeListener 用于统计方法执行时间
> 2. org.beifengtz.jvmm.aop.listener.MethodStackListener 用于采集被增强的方法的调用堆栈

## 方式二：代码调用

org.beifengtz.jvmm.aop.core.Enhancer 类中提供了一些静态方法可以直接使用

例如：对 org.beifengtz.jvmm.common.util.StringUtil 中的以 Days 结尾但又不包含 addDays 的方法进行增强

```java
import java.lang.instrument.Instrumentation;

public class EnhanceMain {
    public static void main(String[] args) {
        String className = "org.beifengtz.jvmm.common.util.StringUtil";
        byte[] classBytes = Enhancer.enhanceMethod(
                className,
                methodListener,
                "*Days",
                "addDays"
        );

        CustomClassLoader classLoader = new CustomClassLoader();
        Class<?> clazz = classLoader.defineClass(className, classBytes);
        
        //  invoke method...
    }

    static class CustomClassLoader extends ClassLoader {
        public Class<?> defineClass(String className, byte[] bytes) {
            return super.defineClass(className, bytes, 0, bytes.length);
        }
    }
}
```

如果你想批量增强多个类时，可以实例化一个Enhance类

```java
import java.lang.instrument.Instrumentation;

public class EnhanceMain {
    public static void premain(Instrumentation instrumentation) {
        new Enhancer(
                "org.beifengtz.jvmm.*.util.*",
                null,
                "get*",
                null,
                methodListener
        ).enhance(instrumentation);
    }
}
```
