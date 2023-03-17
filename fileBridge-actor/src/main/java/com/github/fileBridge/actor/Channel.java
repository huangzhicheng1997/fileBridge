package com.github.fileBridge.actor;

/**
 * @author ZhiCheng
 * @date 2023/3/16 15:18
 */
public interface Channel<T> {

    boolean send(T t);

    T recv();

    boolean isEmpty();

    //void send(T t) throws InterruptedException;

}
