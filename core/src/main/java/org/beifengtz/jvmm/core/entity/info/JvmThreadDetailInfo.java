package org.beifengtz.jvmm.core.entity.info;

import java.lang.Thread.State;

/**
 * description: TODO
 * date: 15:09 2023/4/11
 *
 * @author beifengtz
 */
public class JvmThreadDetailInfo {
    long id;
    String name;
    String group;
    State state;
    Integer osState;
    Boolean daemon;
    Integer priority;
    long userTime;
    long cpuTime;
    long blockedCount;
    long blockedTime;
    long waitedCount;
    long waitedTime;
    String[] locks;

    private JvmThreadDetailInfo(){}

    public static JvmThreadDetailInfo create() {
        return new JvmThreadDetailInfo();
    }

    public long getId() {
        return id;
    }

    public JvmThreadDetailInfo setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public JvmThreadDetailInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public JvmThreadDetailInfo setGroup(String group) {
        this.group = group;
        return this;
    }

    public State getState() {
        return state;
    }

    public JvmThreadDetailInfo setState(State state) {
        this.state = state;
        return this;
    }

    public Integer getOsState() {
        return osState;
    }

    public JvmThreadDetailInfo setOsState(Integer osState) {
        this.osState = osState;
        return this;
    }

    public Boolean getDaemon() {
        return daemon;
    }

    public JvmThreadDetailInfo setDaemon(Boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public Integer getPriority() {
        return priority;
    }

    public JvmThreadDetailInfo setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public long getUserTime() {
        return userTime;
    }

    public JvmThreadDetailInfo setUserTime(long userTime) {
        this.userTime = userTime;
        return this;
    }

    public long getCpuTime() {
        return cpuTime;
    }

    public JvmThreadDetailInfo setCpuTime(long cpuTime) {
        this.cpuTime = cpuTime;
        return this;
    }

    public long getBlockedCount() {
        return blockedCount;
    }

    public JvmThreadDetailInfo setBlockedCount(long blockedCount) {
        this.blockedCount = blockedCount;
        return this;
    }

    public long getBlockedTime() {
        return blockedTime;
    }

    public JvmThreadDetailInfo setBlockedTime(long blockedTime) {
        this.blockedTime = blockedTime;
        return this;
    }

    public long getWaitedCount() {
        return waitedCount;
    }

    public JvmThreadDetailInfo setWaitedCount(long waitedCount) {
        this.waitedCount = waitedCount;
        return this;
    }

    public long getWaitedTime() {
        return waitedTime;
    }

    public JvmThreadDetailInfo setWaitedTime(long waitedTime) {
        this.waitedTime = waitedTime;
        return this;
    }

    public String[] getLocks() {
        return locks;
    }

    public JvmThreadDetailInfo setLocks(String[] locks) {
        this.locks = locks;
        return this;
    }
}
