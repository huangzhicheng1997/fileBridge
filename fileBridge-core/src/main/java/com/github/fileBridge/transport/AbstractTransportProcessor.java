package com.github.fileBridge.transport;

import com.github.fileBridge.common.Event;
import com.github.fileBridge.handler.WaterMarkStatus;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author ZhiCheng
 * @date 2022/12/26 10:11
 */
public abstract class AbstractTransportProcessor implements TransportProcessor {

    protected final int lowWaterMark;
    protected final int highWaterMark;
    protected final BlockingQueue<Event> eventBuffer;
    protected final Loadbalancer loadbalancer;

    public AbstractTransportProcessor(int lowWaterMark, int highWaterMark,
                                      Loadbalancer loadbalancer) {
        this.lowWaterMark = lowWaterMark;
        this.highWaterMark = highWaterMark;
        this.loadbalancer = loadbalancer;
        //todo
        this.eventBuffer = new LinkedBlockingQueue<>();
    }

    @Override
    public void request(Event event) {
        this.eventBuffer.offer(event);
    }

    protected Event poll(long timeout, TimeUnit unit) throws InterruptedException {
        return this.eventBuffer.poll(timeout, unit);
    }

    protected Event poll() {
        return this.eventBuffer.poll();
    }

    protected Event take() throws InterruptedException {
        return this.eventBuffer.take();
    }

    protected Event peek() {
        return this.eventBuffer.peek();
    }


    @Override
    public WaterMarkStatus waterMarkStatus() {
        int size = this.eventBuffer.size();
        if (size < lowWaterMark) {
            return WaterMarkStatus.NORMAL;
        }
        if (size < highWaterMark) {
            return WaterMarkStatus.WARRING;
        }
        return WaterMarkStatus.OVERFLOW;
    }

}
