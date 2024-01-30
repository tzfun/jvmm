package org.beifengtz.jvmm.server.exporter;

import org.beifengtz.jvmm.server.entity.conf.SentinelSubscriberConf;

import java.util.concurrent.CompletableFuture;

/**
 * description: TODO
 * date: 15:45 2024/1/25
 *
 * @author beifengtz
 */
public interface JvmmExporter<T, R> {

    CompletableFuture<R> export(SentinelSubscriberConf subscriber, T data);
}
