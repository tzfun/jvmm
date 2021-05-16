package org.beifengtz.jvmm.client.test;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 12:10 下午 2021/5/16
 *
 * @author beifengtz
 */
public class TestVM {

    @Test
    public void testVmList(){
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        System.out.println(list);
    }
}
