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

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinTask;
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
        JvmmAOPInitializer.initAspect("org.beifengtz", instrumentation);
        int taskCount = 10;

        //  测试包装后的 JDK 线程池
//        testThreadPoolExecutor(taskCount);
        //  测试增强第三方自定义线程池
        testEnhancedThreadPoolExecutor(taskCount);

        testForkJoinTask(taskCount);
    }

    private static void testForkJoinTask(int taskCount) {

    }

    private static void testEnhancedThreadPoolExecutor(int taskCount) throws Exception {
        EventLoopGroup executor = ChannelUtil.newEventLoopGroup(3 * taskCount);

        //  对 netty 的executor增强，其实际实现的execute是在 AbstractEventExecutorGroup 类中实现的
        Class<AbstractEventExecutorGroup> clazz = AbstractEventExecutorGroup.class;
        Instrumentation instrumentation = AgentBootStrap.getInstrumentation();
        instrumentation.redefineClasses(new ClassDefinition(clazz, Enhancer.enhanceExecutor(clazz)));

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
            ThreadLocalStore.setAttributes(new Attributes().setContextId("context_" + i));
            System.out.println("==> post task " + i + ": " + ThreadLocalStore.getAttributes().getContextId());
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
        System.out.println(ThreadLocalStore.getAttributes().getContextId() + " [" + Thread.currentThread().getId() + "] invoke calculate result " + result);
    }
}
