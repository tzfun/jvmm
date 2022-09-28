package org.beifengtz.jvmm.core;

import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.common.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Description: TODO
 *
 * Created in 11:27 2022/9/28
 *
 * @author beifengtz
 */
public class OsProvider {

    static {
        InputStream is = OsProvider.class.getResourceAsStream("/jvmm-extension/libjvmm-sysio.dll");
        if (is != null) {
            File file = new File(JvmmFactory.getTempPath(), "libjvmm-sysio.dll");
            try {
                FileUtil.writeByteArrayToFile(file, IOUtil.toByteArray(is));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.loadLibrary(file.getAbsolutePath());
        }
    }

    public static native void sayHello();

    public static native String load();

    public static void main(String[] args) {
        OsProvider.sayHello();
        System.out.println(OsProvider.load());
    }
}
