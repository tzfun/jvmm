package org.beifengtz.jvmm.client;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:06 2021/5/22
 *
 * @author beifengtz
 */
public class ClientConfigFileMapping {
    private String name;
    private Map<String, String> port = new HashMap<>();

    public String getName() {
        return name;
    }

    public Map<String, String> getPort() {
        return port;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(Map<String, String> port) {
        this.port = port;
    }
}
