package org.beifengtz.jvmm.asm;

/**
 * description: TODO
 * date: 10:29 2023/6/29
 *
 * @author beifengtz
 */
public interface MethodRegexFactory {
    String getMethodRegex(String className);

    String getMethodIgnoreRegex(String className);
}
