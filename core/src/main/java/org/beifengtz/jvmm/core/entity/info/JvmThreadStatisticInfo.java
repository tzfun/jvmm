package org.beifengtz.jvmm.core.entity.info;

import java.lang.Thread.State;

/**
 * description: TODO
 * date: 15:09 2023/4/11
 *
 * @author beifengtz
 */
public class JvmThreadStatisticInfo {
    long id;
    String name;
    State state;
    long userTime;
    long cpuTime;
    long blockedCount;
    long blockedTime;
    long waitedCount;
    long waitedTime;
    String[] locks;

    private JvmThreadStatisticInfo(){}

    public static JvmThreadStatisticInfo create() {
        return new JvmThreadStatisticInfo();
    }

    public long getId() {
        return id;
    }

    public JvmThreadStatisticInfo setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public JvmThreadStatisticInfo setName(String name) {
        this.name = name;
        return this;
    }

    public State getState() {
        return state;
    }

    public JvmThreadStatisticInfo setState(State state) {
        this.state = state;
        return this;
    }

    public long getUserTime() {
        return userTime;
    }

    public JvmThreadStatisticInfo setUserTime(long userTime) {
        this.userTime = userTime;
        return this;
    }

    public long getCpuTime() {
        return cpuTime;
    }

    public JvmThreadStatisticInfo setCpuTime(long cpuTime) {
        this.cpuTime = cpuTime;
        return this;
    }

    public long getBlockedCount() {
        return blockedCount;
    }

    public JvmThreadStatisticInfo setBlockedCount(long blockedCount) {
        this.blockedCount = blockedCount;
        return this;
    }

    public long getBlockedTime() {
        return blockedTime;
    }

    public JvmThreadStatisticInfo setBlockedTime(long blockedTime) {
        this.blockedTime = blockedTime;
        return this;
    }

    public long getWaitedCount() {
        return waitedCount;
    }

    public JvmThreadStatisticInfo setWaitedCount(long waitedCount) {
        this.waitedCount = waitedCount;
        return this;
    }

    public long getWaitedTime() {
        return waitedTime;
    }

    public JvmThreadStatisticInfo setWaitedTime(long waitedTime) {
        this.waitedTime = waitedTime;
        return this;
    }

    public String[] getLocks() {
        return locks;
    }

    public JvmThreadStatisticInfo setLocks(String[] locks) {
        this.locks = locks;
        return this;
    }
}
