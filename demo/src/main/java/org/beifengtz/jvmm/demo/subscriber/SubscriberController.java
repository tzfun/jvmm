package org.beifengtz.jvmm.demo.subscriber;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.convey.annotation.HttpController;
import org.beifengtz.jvmm.convey.annotation.HttpRequest;
import org.beifengtz.jvmm.convey.annotation.RequestBody;
import org.beifengtz.jvmm.convey.enums.Method;
import org.beifengtz.jvmm.core.entity.JvmmData;

/**
 * description TODO
 * date 23:50 2023/6/22
 *
 * @author beifengtz
 */
@HttpController
public class SubscriberController {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SubscriberController.class);

    @HttpRequest(value = "/monitor/subscriber", method = Method.POST)
    public void subscriber(@RequestBody JvmmData data) {
        logger.info("received data: {}", data);
    }
}
