package org.beifengtz.jvmm.core.conf;

import com.google.gson.Gson;
import org.beifengtz.jvmm.common.util.FileUtil;
import org.beifengtz.jvmm.core.conf.entity.LogConf;
import org.beifengtz.jvmm.core.conf.entity.ServerConf;

import java.io.File;
import java.io.IOException;

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

    public static Configuration parseFromUrl(String url) {
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                boolean loaded = FileUtil.readFileFromNet(url, "tmp", "config.yml");
                if (loaded) {
                    return new Gson().fromJson(FileUtil.readYml2Json(new File(url)), Configuration.class);
                } else {
                    throw new RuntimeException("Can not load 'config.yml' from " + url);
                }
            } else {
                return new Gson().fromJson(FileUtil.readYml2Json(new File(url)), Configuration.class);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
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
        return workThread;
    }

    public Configuration setWorkThread(int workThread) {
        this.workThread = workThread;
        return this;
    }
}
