package org.beifengtz.jvmm.demo.enhance;

import org.beifengtz.jvmm.aop.annotation.AspectJoin;
import org.beifengtz.jvmm.aop.core.Attributes;
import org.beifengtz.jvmm.aop.core.ThreadLocalStore;
import org.beifengtz.jvmm.aop.listener.MethodExecuteTimeListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: TODO
 * date: 15:20 2023/10/12
 *
 * @author beifengtz
 */
@AspectJoin(
        classPattern = "org.beifengtz.jvmm.demo.enhance.EnhanceDemo",
        methodPattern = "calculate"
)
public class Listener extends MethodExecuteTimeListener {
    private static final Map<String, List<Node>> invokeInfoMap = new ConcurrentHashMap<>();

    @Override
    protected void onMethodExecute(Node node) {
        Attributes attributes = ThreadLocalStore.getAttributes();
        if (attributes != null) {
            List<Node> invokeList = invokeInfoMap.computeIfAbsent(attributes.getContextId(), o -> new LinkedList<>());
            synchronized (invokeList) {
                invokeList.add(node);
            }
        }
    }

    public static void printTrace() {
        for (Entry<String, List<Node>> entry : invokeInfoMap.entrySet()) {
            List<Node> list = entry.getValue();
            StringBuilder sb = new StringBuilder(entry.getKey());
            sb.append(" trace:");
            for (Node node : list) {
                sb.append(" ==> ")
                        .append("[")
                        .append(node.getThreadId())
                        .append("] ")
                        .append(node.getInfo().getMethodName())
                        .append(node.getInfo().getMethodDesc())
                        .append(" {")
                        .append(node.getNanos())
                        .append("ns}");
            }
            System.out.println(sb);
        }
    }
}
