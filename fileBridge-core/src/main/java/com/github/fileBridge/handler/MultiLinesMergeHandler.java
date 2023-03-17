package com.github.fileBridge.handler;

import com.github.fileBridge.common.Event;
import com.github.fileBridge.event.EventHandlerPipeline;
import com.github.fileBridge.event.EventLoop;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 多行日志合并算法,例如异常堆栈合并为一条日志
 *
 * @author ZhiCheng
 * @date 2022/9/26 11:08
 */
public class MultiLinesMergeHandler implements EventHandler {

    private final Pattern patternOfHead;

    private final EventBuffer eventBuffer = new EventBuffer();

    private final EventLoop eventLoop;

    public MultiLinesMergeHandler(String patternOfHead, EventLoop eventLoop) {
        this.patternOfHead = Pattern.compile(patternOfHead);
        this.eventLoop = eventLoop;
    }

    @Override
    public void handle(Event event, EventHandlerPipeline pipeline) {
        var mergedEvents = compact(event);
        for (Event mergedEvent : mergedEvents) {
            pipeline.mark();
            try {
                pipeline.fireNext(mergedEvent);
            } finally {
                pipeline.reset();
            }
        }

    }

    private List<Event> compact(Event event) {
        var ls = new ArrayList<Event>();
        if (null == event) {
            return ls;
        }

        String line = event.content();
        if (isNewLog(line)) {
            /*
             *  如果当前event为一条新日志
             * 1.尝试推出上一条日志，此时上一条日志已经是一条完整的日志
             * 2.将当前event放入缓冲区，等待被填充
             */
            Event pushedEvent = pushEvent();
            //如果当前event是第一个event则推出的为null
            if (pushedEvent != null) {
                ls.add(pushedEvent);
            }
            eventBuffer.offer(new EventWrapper(true, event));
        } else {
            //如果缓冲字符不是一个合法的日志则不会进行填充。
            // 例如文件是被截断的，开头部分不是一个完整日志，此时开头的日志不会进行合并而是丢弃
            if (isNewLog(eventBuffer.stringBuffer())) {
                eventBuffer.offer(new EventWrapper(false, event));
            }
        }
        /*
         * 如果已经到文件末尾了,说明最后一条日志到此结束，可以把buffer 安全推出
         */
        if (eventLoop.isEOF()) {
            Event pushedEvent = pushEvent();
            if (null != pushedEvent) ls.add(pushedEvent);
        }
        return ls;
    }


    private boolean isNewLog(String log) {
        return patternOfHead.matcher(log).matches();
    }

    /*
     * 推出日志，如果没有可推出的日志返回null
     */
    private Event pushEvent() {
        //如果不存在前一条日志，则说明当前日志为第一条日志，需要等待被填充
        if (eventBuffer.isEmpty()) {
            return null;
        }
        EventWrapper last = eventBuffer.getLast();
        //如果前一个event就是一个标准的日志，可以直接推出
        if (last.isNewLog) {
            Event event = eventBuffer.poll().event;
            eventBuffer.clear();
            return event;
        }
        //如果前一个event不是一个标准日志，例如堆栈信息中的一行，则说明buffer中的数据是需要合并的
        Event mergedEvent = new Event(eventBuffer.stringBuffer(), new HashMap<>(), eventLoop.output(), last.event.offset(), last.event.id());
        eventBuffer.clear();
        return mergedEvent;
    }

    private static class EventBuffer {
        private final ArrayDeque<EventWrapper> eventBuffer = new ArrayDeque<>();
        private StringBuffer sb = new StringBuffer();

        public void offer(EventWrapper event) {
            eventBuffer.offer(event);
            sb.append(event.event.content());
        }

        public String stringBuffer() {
            return sb.toString();
        }

        public boolean isEmpty() {
            boolean empty = eventBuffer.isEmpty();
            if (empty) {
                if (!sb.isEmpty()) {
                    throw new IllegalStateException();
                }
                return empty;
            } else {
                if (sb.isEmpty()) {
                    throw new IllegalStateException();
                }
            }
            return false;
        }

        public void clear() {
            sb = new StringBuffer();
            eventBuffer.clear();
        }

        public EventWrapper getLast() {
            return eventBuffer.getLast();
        }

        public EventWrapper poll() {
            return eventBuffer.poll();
        }
    }


    private record EventWrapper(boolean isNewLog, Event event) {

    }
}
