package org.beifengtz.jvmm.web.manage.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: Fill the hook function before the process ends
 * </p>
 * <p>
 * Created in 2:13 下午 2022/1/11
 *
 * @author beifengtz
 */
@Component
@Slf4j
public class DisposableConfig implements DisposableBean {

    @Override
    public void destroy() throws Exception {
        log.info("Jvmm web server shutdown ......");
    }
}
