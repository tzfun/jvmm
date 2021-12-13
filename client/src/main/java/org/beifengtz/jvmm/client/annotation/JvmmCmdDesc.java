package org.beifengtz.jvmm.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 8:42 下午 2021/12/13
 *
 * @author beifengtz
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JvmmCmdDesc {
    String desc();
}
