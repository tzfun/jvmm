package org.beifengtz.jvmm.convey.annotation;

import org.beifengtz.jvmm.convey.enums.GlobalType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 5:08 下午 2021/5/30
 *
 * @author beifengtz
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JvmmMapping {

    String type() default "";

    GlobalType typeEnum() default GlobalType.JVMM_TYPE_HANDLE_MSG;
}
