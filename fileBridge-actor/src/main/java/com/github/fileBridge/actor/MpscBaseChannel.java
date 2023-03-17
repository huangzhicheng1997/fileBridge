package com.github.fileBridge.actor;

import org.jctools.queues.MpscUnboundedArrayQueue;

/**
 * @author ZhiCheng
 * @date 2023/3/16 15:55
 */
public class MpscBaseChannel<T> implements Channel<T> {

    private final MpscUnboundedArrayQueue<T> channel = new MpscUnboundedArrayQueue<>(128);

    @Override
    public boolean send(T t) {
        return channel.offer(t);
    }

    @Override
    public T recv() {
        return channel.poll();
    }

    @Override
    public boolean isEmpty() {
        return channel.isEmpty();
    }
}
