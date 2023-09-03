package org.beifengtz.jvmm.server.entity.conf;

import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:48 2021/5/22
 *
 * @author beifengtz
 */
public final class Configuration {

    private String name = "jvmm-server";
    private ServerConf server = new ServerConf();
    private LogConf log = new LogConf();

    private int workThread = 2;

    /**
     * 从一个地址中载入配置，这个地址需要指定到一个yml文件，配置格式需满足与 Configuration 类一一对应，
     * 这个地址的格式，可以是一个http或https的网络地址，也可以是一个文件路径
     *
     * @param url 文件地址，可以是https(s)地址也可以是本地文件地址
     * @return {@link Configuration}对象
     */
    public static Configuration parseFromUrl(String url) {
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                boolean loaded = FileUtil.readFileFromNet(url, "tmp", "config.yml");
                if (loaded) {
                    return parseFromYamlFile(new File("tmp", "config.yml"));
                } else {
                    throw new RuntimeException("Can not load 'config.yml' from " + url);
                }
            } else {
                File file = new File(url);
                if (file.exists()) {
                    return parseFromYamlFile(file);
                } else {
                    System.err.println("Can not load config from not exist file: " + url);
                    return null;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Configuration parseFromStream(InputStream is) {
        Yaml yaml = new Yaml();
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml.loadAs(is, Configuration.class);
    }

    public static Configuration parseFromYamlStr(String ymlStr) {
        Yaml yaml = new Yaml();
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml.loadAs(ymlStr, Configuration.class);
    }

    public static Configuration parseFromYamlFile(File file) throws IOException {
        Yaml yaml = new Yaml();
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml.loadAs(Files.newInputStream(file.toPath()), Configuration.class);
    }

    @Override
    public String toString() {
        return StringUtil.getGson().toJson(this);
    }

    public String getName() {
        return name;
    }

    public Configuration setName(String name) {
        this.name = name;
        return this;
    }

    public ServerConf getServer() {
        return server;
    }

    public Configuration setServer(ServerConf server) {
        this.server = server;
        return this;
    }

    public LogConf getLog() {
        return log;
    }

    public Configuration setLog(LogConf log) {
        this.log = log;
        return this;
    }

    public int getWorkThread() {
        return Math.max(2, workThread);
    }

    public Configuration setWorkThread(int workThread) {
        this.workThread = workThread;
        return this;
    }
}
