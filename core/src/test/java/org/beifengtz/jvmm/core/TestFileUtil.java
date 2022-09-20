package org.beifengtz.jvmm.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 15:10 2022/9/7
 *
 * @author beifengtz
 */
public class TestFileUtil {
    @Test
    public void testYml2Json() throws IOException {
        JsonObject json = FileUtil.readYml2Json(new File("/Users/beifengtz/Program/jvmm/server/src/main/resources/config.yml"));
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        System.out.println(gson.toJson(json));
    }
}
