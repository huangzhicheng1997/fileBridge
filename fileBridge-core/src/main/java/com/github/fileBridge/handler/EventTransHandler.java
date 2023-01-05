package com.github.fileBridge.handler;

import com.github.fileBridge.common.Event;
import com.github.fileBridge.event.EventHandlerPipeline;
import com.github.fileBridge.event.EventLoop;
import com.github.fileBridge.transport.TransportProcessor;


/**
 * @author ZhiCheng
 * @date 2022/12/20 14:31
 */
public class EventTransHandler implements EventHandler {

    private final TransportProcessor transportProcessor;
    private final EventLoop eventLoop;

    public EventTransHandler(TransportProcessor transportProcessor, EventLoop eventLoop) {
        this.transportProcessor = transportProcessor;
        this.eventLoop = eventLoop;
        this.eventLoop.getBootLoader().slowCycle.add(() -> {
            if (transportProcessor.waterMarkStatus() == WaterMarkStatus.NORMAL) {
                eventLoop.resume();
            }
        });
    }

    @Override
    public void handle(Event event, EventHandlerPipeline pipeline) {
        if (null == event) {
            return;
        }
        WaterMarkStatus waterMarkStatus = transportProcessor.waterMarkStatus();
        switch (waterMarkStatus) {
            case WARRING -> eventLoop.suspendNext(1000);
            case OVERFLOW -> eventLoop.suspendNext();
        }
        transportProcessor.request(event);
    }
}
