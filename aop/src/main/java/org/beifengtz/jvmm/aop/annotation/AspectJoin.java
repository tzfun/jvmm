package org.beifengtz.jvmm.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description: 此注解需添加在一个 {@link org.beifengtz.jvmm.aop.core.MethodListener} 实现类上，此类会被解析为单例
 * date: 15:09 2023/6/30
 *
 * @author beifengtz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AspectJoin {
    /**
     * 类匹配符，通配符为*，例如：
     * <pre>
     *     1. org.beifengtz.jvmm.*.util.RegexUtil 匹配 org.beifengtz.jvmm.common.util.RegexUtil
     *     2. org.beifengtz.jvmm.common.util.*Util 匹配 org.beifengtz.jvmm.common.util.RegexUtil
     * </pre>
     *
     * @return 类匹配符
     */
    String classPattern();

    /**
     * 需要忽略的类匹配符，规则同 {@link #classPattern()}
     *
     * @return 忽略类匹配符
     */
    String classIgnorePattern() default "";

    /**
     * 需切入的方法，默认全部方法
     *
     * @return 方法名匹配符
     */
    String methodPattern() default "*";

    /**
     * 需忽略的方法名，默认不忽略
     *
     * @return 方法名匹配符
     */
    String methodIgnorePattern() default "";
}
