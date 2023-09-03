package org.beifengtz.jvmm.core.entity.info;

import org.beifengtz.jvmm.common.JsonParsable;

import java.lang.Thread.State;

/**
 * description TODO
 * date 15:23 2023/9/3
 *
 * @author beifengtz
 */
public class ThreadTimedInfo implements JsonParsable {
    private long id;
    private String name;
    private String group;
    private State state;
    private Boolean daemon;
    private Integer priority;
    private long userTime;
    private long cpuTime;

    @Override
    public String toString() {
        return toJsonStr();
    }

    public long getId() {
        return id;
    }

    public ThreadTimedInfo setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ThreadTimedInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public ThreadTimedInfo setGroup(String group) {
        this.group = group;
        return this;
    }

    public State getState() {
        return state;
    }

    public ThreadTimedInfo setState(State state) {
        this.state = state;
        return this;
    }

    public Boolean getDaemon() {
        return daemon;
    }

    public ThreadTimedInfo setDaemon(Boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public Integer getPriority() {
        return priority;
    }

    public ThreadTimedInfo setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public long getUserTime() {
        return userTime;
    }

    public ThreadTimedInfo setUserTime(long userTime) {
        this.userTime = userTime;
        return this;
    }

    public long getCpuTime() {
        return cpuTime;
    }

    public ThreadTimedInfo setCpuTime(long cpuTime) {
        this.cpuTime = cpuTime;
        return this;
    }
}
