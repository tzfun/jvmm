package org.beifengtz.jvmm.aop.listener;

import org.beifengtz.jvmm.aop.core.MethodInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * description: 以某一些方法作为切入点，对所有增强过的方法进行用时统计，并最终生成调用堆栈
 * date: 17:38 2023/7/6
 *
 * @author beifengtz
 */
public class MethodStackListener extends MethodExecuteTimeListener {

    public static class MethodStack {
        /**
         * class path的层级
         */
        final int level;
        final List<Node> nodeList = new ArrayList<>();
        MethodStack next = null;

        public MethodStack(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        public List<Node> getNodeList() {
            return nodeList;
        }

        public MethodStack getNext() {
            return next;
        }
    }

    private final ThreadLocal<Deque<Node>> callerStack = ThreadLocal.withInitial(LinkedList::new);
    private final ThreadLocal<Boolean> needLogStack = ThreadLocal.withInitial(() -> false);

    protected final String joinClassPattern;
    protected final String joinMethodPattern;

    public MethodStackListener(String joinClassPattern) {
        this(joinClassPattern, null);
    }

    /**
     * @param joinClassPattern  记录调用堆栈的启动类匹配符，*为通配符
     * @param joinMethodPattern 记录调用堆栈的启动类下的方法匹配符，*为通配符，如果为 null 默认为 *
     */
    public MethodStackListener(String joinClassPattern, String joinMethodPattern) {
        this.joinClassPattern = joinClassPattern.replaceAll("\\.", ".*");
        this.joinMethodPattern = joinMethodPattern == null ? ".*" : joinMethodPattern.replaceAll("\\.", ".*");
    }

    @Override
    public void before(MethodInfo info) throws Throwable {
        super.before(info);
        if (!needLogStack.get() && info.getClassName().matches(joinClassPattern) && info.getMethodName().matches(joinMethodPattern)) {
            needLogStack.set(true);
        }
    }

    @Override
    public void after(MethodInfo info, Object returnVal, Throwable throwable) throws Throwable {
        super.after(info, returnVal, throwable);
        if (execStack.get().isEmpty() && needLogStack.get()) {
            needLogStack.set(false);
            Deque<Node> callers = callerStack.get();

            int level = 1;
            String curLevelClassPath = null;
            MethodStack head = new MethodStack(level);
            Node node = callers.pop();
            curLevelClassPath = node.info.getClassName();
            head.nodeList.add(node);

            MethodStack tmp = head;
            while (callers.peek() != null) {
                node = callers.pop();
                if (!node.info.getClassName().equals(curLevelClassPath)) {
                    MethodStack ms = new MethodStack(++level);
                    tmp.next = ms;
                    tmp = ms;
                    curLevelClassPath = node.info.getClassName();
                }
                tmp.nodeList.add(node);
            }
            onMethodStack(head);
        }
    }

    @Override
    protected void onMethodExecute(Node node) {
        if (needLogStack.get()) {
            callerStack.get().push(node);
        }
    }

    /**
     * 当一个方法执行结束时触发此函数
     *
     * @param stack {@link MethodStack}包含此方法执行期间调用过的所有【已被增强过的方法】的堆栈以及其 Node 信息
     */
    protected void onMethodStack(MethodStack stack) {
        StringBuilder sb = new StringBuilder("Execute stack:\n");
        MethodStack tmp = stack;
        while (tmp != null) {
            for (Node node : tmp.getNodeList()) {
                for (int i = 0; i < tmp.level - 1; i++) {
                    sb.append("\t");
                }
                //  ms
                String useTime = BigDecimal.valueOf(node.nanos / 1000_000.0).toPlainString();

                sb.append("--[").append(useTime).append(" ms");

                int fillBlack = 10 - useTime.length();
                for (int i = 0; i < fillBlack; i++) {
                    sb.append(" ");
                }

                sb.append("]")
                        .append(node.info.getClassName())
                        .append("#")
                        .append(node.info.getMethodName())
                        .append(node.info.getMethodDesc());
                sb.append("\n");
            }
            tmp = tmp.next;
        }
        sb.deleteCharAt(sb.length() - 1);
        System.out.println(sb);
    }

}
