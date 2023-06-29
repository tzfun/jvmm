package org.beifengtz.jvmm.asm;

/**
 * description: 方法过滤正则表达式工厂接口
 * date: 10:29 2023/6/29
 *
 * @author beifengtz
 */
public interface MethodRegexFactory {
    /**
     * 获取指定类中需要增强的方法正则表达式
     *
     * @param className 类全限名
     * @return 正则表达式
     */
    String getMethodRegex(String className);

    /**
     * 获取指定类中需要忽略增强的方法正则表达式
     *
     * @param className 类权限名
     * @return 正则表达式
     */
    String getMethodIgnoreRegex(String className);
}
