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
 * Created in 3:22 下午 2021/12/12
 *
 * @author beifengtz
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JvmmOption {

    String name();

    boolean hasArg() default false;

    String argName() default "";

    boolean required() default false;

    String desc() default "";

}
