package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.core.entity.mx.MemoryInfo;
import org.beifengtz.jvmm.core.entity.mx.SystemDynamicInfo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:58 2021/5/12
 *
 * @author beifengtz
 */
public class TestCollector {
    @Test
    public void testJsonParser() {
        JvmmCollector collector = JvmmFactory.getCollector();
        MemoryInfo info = collector.getMemory();
        System.err.println(info.toJsonStr());
        System.err.println(info);
    }

    @Test
    public void testSystemDynamic() {
        SystemDynamicInfo systemDynamic = JvmmFactory.getCollector().getSystemDynamic();
        System.out.println(systemDynamic);
    }

    @Test
    public void testParseFree(){
        String output = "Mem:        8170260     5654384      598400       12160     1917476     2198144";
        String[] split = output.split("\\s+");
        System.out.println(Arrays.toString(split));
    }
}
