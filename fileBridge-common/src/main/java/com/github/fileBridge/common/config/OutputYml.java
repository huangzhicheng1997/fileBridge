package com.github.fileBridge.common.config;

/**
 * @author ZhiCheng
 * @date 2022/10/31 14:26
 */
public final class OutputYml {
    private String dir;
    private String pattern;
    private String logPattern;
    //latest  offset  fromHead
    private String readStrategy = "latest";
    private String addresses;
    private String invalidateTime;
    private String scriptName;
    private String waterMark = "512-1024";
    private String transTimeout = "300";

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public String getReadStrategy() {
        return readStrategy;
    }

    public void setReadStrategy(String readStrategy) {
        this.readStrategy = readStrategy;
    }

    public String getLogPattern() {
        return logPattern;
    }

    public void setLogPattern(String logPattern) {
        this.logPattern = logPattern;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getInvalidateTime() {
        return invalidateTime;
    }

    public void setInvalidateTime(String invalidateTime) {
        this.invalidateTime = invalidateTime;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getWaterMark() {
        return waterMark;
    }

    public void setWaterMark(String waterMark) {
        this.waterMark = waterMark;
    }

    public String getTransTimeout() {
        return transTimeout;
    }

    public void setTransTimeout(String transTimeout) {
        this.transTimeout = transTimeout;
    }
}
