package com.github.fileBridge.transport;

import com.github.fileBridge.common.proto.ResOuterClass;

/**
 * @author ZhiCheng
 * @date 2022/12/19 15:47
 */
public class ObserverContext {
    public ResOuterClass.Res res;
    public Throwable throwable;
    public boolean completed;
}
