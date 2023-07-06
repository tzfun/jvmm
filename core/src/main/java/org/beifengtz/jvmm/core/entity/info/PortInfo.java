package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * description: TODO
 * date: 9:39 2023/7/6
 *
 * @author beifengtz
 */
public class PortInfo implements JsonParsable {
    private final List<Integer> running = new ArrayList<>();
    private final List<Integer> stopped = new ArrayList<>();


    public List<Integer> getRunning() {
        return running;
    }

    public List<Integer> getStopped() {
        return stopped;
    }

    public PortInfo addRunning(int port) {
        running.add(port);
        return this;
    }

    public PortInfo addAllRunning(Collection<Integer> ports) {
        running.addAll(ports);
        return this;
    }

    public PortInfo clearRunning() {
        running.clear();
        return this;
    }

    public PortInfo addStopped(int port) {
        stopped.add(port);
        return this;
    }

    public PortInfo addAllStopped(Collection<Integer> ports) {
        stopped.addAll(ports);
        return this;
    }

    public PortInfo clearStopped() {
        stopped.clear();
        return this;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
