package com.github.fileBridge.handler;

import com.github.fileBridge.OffsetRecorder;
import com.github.fileBridge.common.Event;
import com.github.fileBridge.event.EventHandlerPipeline;

/**
 * @author ZhiCheng
 * @date 2022/12/24 21:35
 */
public class CommitControlHandler implements EventHandler {

    private final OffsetRecorder offsetRecorder;

    public CommitControlHandler(OffsetRecorder offsetRecorder) {
        this.offsetRecorder = offsetRecorder;
    }

    @Override
    public void handle(Event event, EventHandlerPipeline pipeline) {
        offsetRecorder.commitOffset(event);
        pipeline.fireNext(event);
    }
}
