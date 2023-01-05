package com.github.fileBridge.transport;

import com.github.fileBridge.common.Event;
import com.github.fileBridge.handler.WaterMarkStatus;

public interface TransportProcessor {

    void request(Event event);

    WaterMarkStatus waterMarkStatus();

    void start();

    void shutdown();


}
