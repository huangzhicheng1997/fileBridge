package com.github.fileBridge.event;

import com.github.fileBridge.common.Event;
import com.github.fileBridge.handler.EventHandler;

import java.util.List;

/**
 * @author ZhiCheng
 * @date 2022/11/7 16:44
 */
public class EventHandlerPipeline {
    private final Node head = new Node();
    private Node tail = head;
    private Node curr;
    private Node mark;

    public EventHandlerPipeline(List<EventHandler> handlers) {
        if (handlers.size() == 0) {
            return;
        }
        for (EventHandler handler : handlers) {
            Node node = new Node(handler, null);
            tail.next = node;
            tail = node;
        }
        curr = head;
    }

    public void addLast(EventHandler handler) {
        Node node = new Node(handler, null);
        tail.next = node;
        tail = node;
    }

    public void fireNext(Event event) {
        curr = curr.next;
        if (curr == null) {
            return;
        }
        curr.handler.handle(event, this);
    }

    public void mark() {
        this.mark = this.curr;
    }

    public void reset() {
        this.curr = this.mark;
    }


    private static class Node {
        EventHandler handler;
        Node next;

        public Node() {
        }

        public Node(EventHandler handler, Node next) {
            this.handler = handler;
            this.next = next;
        }
    }
}
