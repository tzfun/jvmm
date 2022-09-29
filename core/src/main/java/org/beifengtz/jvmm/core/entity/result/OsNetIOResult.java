package org.beifengtz.jvmm.core.entity.result;

import org.beifengtz.jvmm.common.JsonParsable;

/**
 * Description: TODO
 *
 * Created in 15:02 2022/9/29
 *
 * @author beifengtz
 */
public class OsNetIOResult implements JsonParsable {
    /**
     * 收包数量
     */
    private long receivePackageCount;
    /**
     * 发包数量
     */
    private long transmitPackageCount;
    /**
     * 收包速度，B/s
     */
    private float receiveSpeed;
    /**
     * 发包速度，B/s
     */
    private float transmitSpeed;

    public long getReceivePackageCount() {
        return receivePackageCount;
    }

    public OsNetIOResult setReceivePackageCount(long receivePackageCount) {
        this.receivePackageCount = receivePackageCount;
        return this;
    }

    public long getTransmitPackageCount() {
        return transmitPackageCount;
    }

    public OsNetIOResult setTransmitPackageCount(long transmitPackageCount) {
        this.transmitPackageCount = transmitPackageCount;
        return this;
    }

    public float getReceiveSpeed() {
        return receiveSpeed;
    }

    public OsNetIOResult setReceiveSpeed(float receiveSpeed) {
        this.receiveSpeed = receiveSpeed;
        return this;
    }

    public float getTransmitSpeed() {
        return transmitSpeed;
    }

    public OsNetIOResult setTransmitSpeed(float transmitSpeed) {
        this.transmitSpeed = transmitSpeed;
        return this;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
