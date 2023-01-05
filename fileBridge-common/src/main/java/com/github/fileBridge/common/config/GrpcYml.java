package com.github.fileBridge.common.config;

/**
 * @author ZhiCheng
 * @date 2022/12/27 17:42
 */
public class GrpcYml {
    private int poolSize=4;

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}
