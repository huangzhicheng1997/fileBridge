package com.github.fileBridge.actor;

import javax.naming.Context;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * @author ZhiCheng
 * @date 2023/3/16 15:18
 */
public interface Actor<T> {

    String id();

    /**
     * 绑定通信通道
     */
    void channel(Supplier<Channel<T>> channelFactory);

    void start();

    void recv(T m);


}
