package org.beifengtz.jvmm.convey.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description TODO
 * date 21:26 2023/7/9
 *
 * @author beifengtz
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestAttr {
    String value() default "";
}
