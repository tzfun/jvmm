package org.beifengtz.jvmm.convey.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 11:27 2022/9/13
 *
 * @author beifengtz
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpController {
    String name() default "";
}
