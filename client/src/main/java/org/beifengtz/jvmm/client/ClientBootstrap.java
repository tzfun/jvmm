package org.beifengtz.jvmm.client;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.beifengtz.jvmm.core.JvmmFactory;
import org.beifengtz.jvmm.core.entity.mx.ClassLoadingInfo;
import org.beifengtz.jvmm.tools.util.CommonUtil;
import org.beifengtz.jvmm.tools.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:11 2021/5/22
 *
 * @author beifengtz
 */
public class ClientBootstrap {
    private static volatile ClientBootstrap clientBootstrap;
    private final ClientConfiguration config;
    private int port = -1;

    private ClientBootstrap() {
        this(parseConfig(null));
    }

    private ClientBootstrap(ClientConfiguration config) {
        this.config = config;
    }

    public synchronized static ClientBootstrap getInstance(Instrumentation inst, String args) throws Throwable {
        if (clientBootstrap != null) {
            return clientBootstrap;
        }
        clientBootstrap = new ClientBootstrap(parseConfig(args));
        return clientBootstrap;
    }

    private static ClientConfiguration parseConfig(String args) {
        if (args == null) {
            args = "";
        }
        Map<String, String> argMap = new HashMap<>(4);
        String[] argArray = args.split(";");
        for (String arg : argArray) {
            String[] kv = arg.split("=");
            String k = kv[0].trim();
            String v = kv[1].trim();
            argMap.put(k, v);
        }
        ClientConfiguration.Builder cb = ClientConfiguration.newBuilder();
        String configPath = argMap.get("config");
        if (Objects.nonNull(configPath)) {
            File configFile = new File(configPath);
            if (!configFile.exists()) {
                throw new RuntimeException("Can not found config file from args: " + configPath);
            }

            try {
                String lowerCase = configPath.toLowerCase(Locale.ROOT);
                if (lowerCase.endsWith("yml") || lowerCase.endsWith("yaml")) {
                    ClientConfigFileMapping mapping = FileUtil.readYml(configFile.getAbsolutePath(), ClientConfigFileMapping.class);
                    cb.merge(mapping);
                } else if (lowerCase.endsWith("properties")) {
                    Map<String, String> propMap = FileUtil.readProperties(configFile.getAbsolutePath());
                    propMap.putAll(argMap);
                    argMap = propMap;
                }
            } catch (IOException e) {
                throw new RuntimeException("An error occurred while reading the file. " + e.getMessage(), e);
            }

        }

        String name = argMap.get("name");
        if (Objects.nonNull(name)) {
            cb.setName(name);
        }

        String portBind = argMap.get("port.bind");
        if (Objects.nonNull(portBind)) {
            cb.setPort(Integer.parseInt(portBind));
        }

        String portAutoIncrease = argMap.get("port.autoIncrease");
        if (Objects.nonNull(portAutoIncrease)) {
            cb.setAutoIncrease(Boolean.parseBoolean(portAutoIncrease));
        }

        return cb.build();
    }

    public int getPort() {
        return port;
    }

    public static void main(String[] args) throws Throwable {

        ClientWatchDog.getInstance().initToolsClasses();

        ClassLoadingInfo classLoading = JvmmFactory.getCollector().getClassLoading();
        CommonUtil.print("-->", classLoading);

        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        list.forEach(o -> CommonUtil.print("==>", o));
    }
}
