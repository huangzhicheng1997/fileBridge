package com.github.fileBridge.common.config;

import com.github.fileBridge.common.utils.FileUtil;
import com.github.fileBridge.common.utils.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * @author ZhiCheng
 * @date 2022/10/31 14:20
 */
public class AgentConfig {

    private String loglevel;

    private String logDir;

    private String schedulerThreads = "1";

    private Map<String, OutputYml> output;

    private GrpcYml rpc;

    public Map<String, OutputYml> getOutput() {
        return output;
    }

    public void setOutput(Map<String, OutputYml> output) {
        this.output = output;
    }

    public static AgentConfig loadProperties() throws IOException {
        String configPath = System.getProperty("log.agent.config.path");
        File file;
        if (StringUtils.isBlank(configPath)) {
            File dir = FileUtil.newFile(System.getProperty("user.dir"));
            file = FileUtil.newFile(dir.getParentFile().getAbsolutePath(), "agent.yaml");
        } else {
            file = new File(configPath);
        }
        if (!file.exists()) {
            throw new FileNotFoundException(file.getPath());
        }
        Yaml yaml = new Yaml();
        return yaml.loadAs(new FileInputStream(file), AgentConfig.class);
    }

    public String getLoglevel() {
        return loglevel;
    }

    public void setLoglevel(String loglevel) {
        this.loglevel = loglevel;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public String getSchedulerThreads() {
        return schedulerThreads;
    }

    public GrpcYml getRpc() {
        return rpc;
    }

    public void setRpc(GrpcYml rpc) {
        this.rpc = rpc;
    }

    public void setSchedulerThreads(String schedulerThreads) {
        this.schedulerThreads = schedulerThreads;
    }
}

