package org.beifengtz.jvmm.core.conf;

import org.beifengtz.jvmm.common.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:36 上午 2021/12/11
 *
 * @author beifengtz
 */
public class ConfigParser {

    /**
     * 解析attach传过来的参数
     *
     * 优先级：
     * 1. 如果参数中有配置属性优先取参数中的配置（即使 config 指定的配置文件中设有相同的属性，仍优先取参数中的值）；
     * 2. 如果参数中指定了 config 参数，（此参数指定向一个配置文件，格式可以是 yml 也可以是 properties），从配置中读取；
     * 3. 如果既没有指定配置文件又没有参数配置，则读默认配置，默认配置：{@link Configuration.Builder}。
     *
     * @param args 参数格式：name=jvmm_server;port.bind=5010;port.autoIncrease=true;http.maxChunkSize=52428800
     * @return {@link Configuration}
     */
    public static Configuration parseFromArgs(String args) {
        if (args == null) {
            args = "";
        }
        Map<String, String> argMap = new HashMap<>(4);
        String[] argArray = args.split(";");
        for (String arg : argArray) {
            String[] kv = arg.split("=");
            if (kv.length > 1) {
                String k = kv[0].trim();
                String v = kv[1].trim();
                argMap.put(k, v);
            }
        }
        Configuration.Builder cb = Configuration.newBuilder();
        String configPath = argMap.get("config");
        if (Objects.nonNull(configPath)) {
            File configFile = new File(configPath);
            if (!configFile.exists()) {
                throw new RuntimeException("Can not found config file from args: " + configPath);
            }

            try {
                String lowerCase = configPath.toLowerCase(Locale.ROOT);
                if (lowerCase.endsWith("yml") || lowerCase.endsWith("yaml")) {
                    ConfigFileMapping mapping = FileUtil.readYml(configFile.getAbsolutePath(), ConfigFileMapping.class);
                    cb.mergeFromMapping(mapping);
                } else if (lowerCase.endsWith("properties")) {
                    Map<String, String> propMap = FileUtil.readProperties(configFile.getAbsolutePath());
                    propMap.putAll(argMap);
                    argMap = propMap;
                }
            } catch (IOException e) {
                throw new RuntimeException("An error occurred while reading the file. " + e.getMessage(), e);
            }
        }

        cb.mergeFromProperties(argMap);

        return cb.build();
    }
}
