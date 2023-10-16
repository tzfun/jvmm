package org.beifengtz.jvmm.aop.listener;

import org.beifengtz.jvmm.aop.core.MethodInfo;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * description: 统计方法执行时间的监听器，可直接使用
 * date: 17:25 2023/7/6
 *
 * @author beifengtz
 */
public class MethodExecuteTimeListener extends MethodListener {
    public static class Node {
        MethodInfo info;
        long nanos;
        long threadId;

        public Node(MethodInfo info, long nanos) {
            this(info, nanos, Thread.currentThread().getId());
        }

        public Node(MethodInfo info, long nanos, long threadId) {
            this.info = info;
            this.nanos = nanos;
            this.threadId = threadId;
        }

        public MethodInfo getInfo() {
            return info;
        }

        public long getNanos() {
            return nanos;
        }

        public long getThreadId() {
            return threadId;
        }
    }

    protected final ThreadLocal<Map<String, Deque<Node>>> execStack = ThreadLocal.withInitial(HashMap::new);

    @Override
    public void before(MethodInfo info) throws Throwable {
        super.before(info);
        Deque<Node> stack = execStack.get().computeIfAbsent(info.key(), o -> new LinkedList<>());
        stack.push(new Node(info, System.nanoTime()));
    }

    @Override
    public void after(MethodInfo info, Object returnVal, Throwable throwable) throws Throwable {
        super.after(info, returnVal, throwable);
        String methodKey = info.key();
        Deque<Node> stack = execStack.get().get(methodKey);
        Node curNode = stack.pop();
        curNode.nanos = System.nanoTime() - curNode.nanos;

        onMethodExecute(curNode);
        if (stack.isEmpty()) {
            execStack.get().remove(methodKey);
        }
    }

    /**
     * 当一个方法执行结束时触发此函数
     *
     * @param node {@link Node}信息，其中包含方法信息和方法执行纳秒数
     */
    protected void onMethodExecute(Node node) {
        System.out.println(node.info.key() + " [" + node.nanos + " ns]");
    }
}
