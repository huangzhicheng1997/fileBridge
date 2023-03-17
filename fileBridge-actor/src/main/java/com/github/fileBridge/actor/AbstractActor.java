package com.github.fileBridge.actor;

import java.util.function.Supplier;

/**
 * @author ZhiCheng
 * @date 2023/3/16 16:36
 */
public class AbstractActor<T> implements Actor<T>{
    @Override
    public String id() {
        return null;
    }

    @Override
    public void channel(Supplier<Channel<T>> channelFactory) {

    }

    @Override
    public void start() {

    }

    @Override
    public void recv(T m) {

    }
}
