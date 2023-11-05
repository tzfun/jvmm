package org.beifengtz.jvmm.demo.enhance;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.AbstractEventExecutorGroup;
import org.beifengtz.jvmm.agent.AgentBootStrap;
import org.beifengtz.jvmm.aop.JvmmAOPInitializer;
import org.beifengtz.jvmm.aop.core.Attributes;
import org.beifengtz.jvmm.aop.core.Enhancer;
import org.beifengtz.jvmm.aop.core.ThreadLocalStore;
import org.beifengtz.jvmm.common.util.AssertUtil;
import org.beifengtz.jvmm.convey.channel.ChannelUtil;

import java.io.File;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * description: need to add javaagent: agent/build/libs/jvmm-agent.jar
 * date: 15:08 2023/10/12
 *
 * @author beifengtz
 */
public class EnhanceDemo {
    public static void main(String[] args) throws Exception {
        Instrumentation instrumentation = AgentBootStrap.getInstrumentation();
        AssertUtil.notNull(instrumentation, "Instrumentation is null, please add JVM argument `javaagent`");
        JvmmAOPInitializer.initAspect("org.beifengtz.jvmm.demo", instrumentation);
        int taskCount = 10;

        //  测试包装后的 JDK 线程池
//        testThreadPoolExecutor(taskCount);
        //  测试增强第三方自定义线程池
        testEnhancedThreadPoolExecutor(taskCount);
        //  测试增强ForkJoin
//        testForkJoinTask(taskCount);
    }

    private static void testForkJoinTask(int taskCount) throws Exception {
        testTrace(taskCount, command -> ForkJoinPool.commonPool().execute(command));
    }

    private static void testEnhancedThreadPoolExecutor(int taskCount) throws Exception {
        EventLoopGroup executor = ChannelUtil.newEventLoopGroup(3 * taskCount);

        //  对 netty 的executor增强，其实际实现的execute是在 AbstractEventExecutorGroup 类中实现的
        Class<AbstractEventExecutorGroup> clazz = AbstractEventExecutorGroup.class;
        Instrumentation instrumentation = AgentBootStrap.getInstrumentation();
        byte[] bytes = Enhancer.enhanceExecutor(clazz);
        Files.write(new File("AbstractEventExecutorGroup.class").toPath(), bytes);
        instrumentation.redefineClasses(new ClassDefinition(clazz, bytes));

        testTrace(taskCount, executor);
        executor.shutdownGracefully();
    }

    private static void testThreadPoolExecutor(int taskCount) throws Exception {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(3 * taskCount, Integer.MAX_VALUE, 10,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>());
        testTrace(taskCount, executor);
        executor.shutdown();
    }

    private static void testTrace(int taskCount, Executor executor) throws Exception {
        CountDownLatch cdl = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            ThreadLocalStore.setAttributes(new Attributes().setContextId("task_context_" + i));
            System.out.println("main thread post task " + i + " with trace id " + ThreadLocalStore.getAttributes().getContextId());
            executor.execute(new Task1(executor, cdl));
        }

        cdl.await();
        Listener.printTrace();
    }

    public static void calculate(int value) {
        long result = 1;
        if (value > 1) {
            for (int i = 2; i <= value; i++) {
                result *= i;
            }
        }
        System.out.println("thread " + Thread.currentThread().getId() + " execute task " + ThreadLocalStore.getAttributes().getContextId() + " and got result " + result);
    }
}
