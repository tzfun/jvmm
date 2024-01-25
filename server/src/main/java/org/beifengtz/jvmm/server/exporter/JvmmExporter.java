package org.beifengtz.jvmm.server.exporter;

import org.beifengtz.jvmm.server.entity.conf.SentinelSubscriberConf;

/**
 * description: TODO
 * date: 15:45 2024/1/25
 *
 * @author beifengtz
 */
public interface JvmmExporter<T> {

    void export(SentinelSubscriberConf subscriber, T data);
}
