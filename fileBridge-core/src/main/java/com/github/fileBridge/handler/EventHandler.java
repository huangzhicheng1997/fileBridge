package com.github.fileBridge.handler;


import com.github.fileBridge.common.Event;
import com.github.fileBridge.event.EventHandlerPipeline;

/**
 * 对文件中读到的数据进行处理
 */
public interface EventHandler {

    void handle(Event event, EventHandlerPipeline pipeline);


}
