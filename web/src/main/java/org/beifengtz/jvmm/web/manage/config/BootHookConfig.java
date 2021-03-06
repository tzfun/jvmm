package org.beifengtz.jvmm.web.manage.config;

import lombok.extern.slf4j.Slf4j;
import org.beifengtz.jvmm.common.factory.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: Fill the hook function after the process started
 * </p>
 * <p>
 * Created in 2:19 下午 2022/1/11
 *
 * @author beifengtz
 */
@Component
@Slf4j
public class BootHookConfig implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LoggerFactory.register(org.slf4j.LoggerFactory.getILoggerFactory());
    }
}
