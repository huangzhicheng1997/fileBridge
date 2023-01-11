package com.github.fileBridge.handler;

import com.github.fileBridge.OffsetRepository;
import com.github.fileBridge.common.Event;
import com.github.fileBridge.event.EventHandlerPipeline;

/**
 * @author ZhiCheng
 * @date 2022/12/24 21:35
 */
public class CommitControlHandler implements EventHandler {

    private final OffsetRepository offsetRepository;

    public CommitControlHandler(OffsetRepository offsetRepository) {
        this.offsetRepository = offsetRepository;
    }

    @Override
    public void handle(Event event, EventHandlerPipeline pipeline) {
        offsetRepository.commitOffset(event);
        pipeline.fireNext(event);
    }
}
