package org.beifengtz.jvmm.client;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 14:26 2021/5/22
 *
 * @author beifengtz
 */
public class ClientConfig {

    private static volatile boolean INIT = false;

    public static void load(){
        INIT = true;
    }

    public static boolean isInited(){
        return INIT;
    }
}
