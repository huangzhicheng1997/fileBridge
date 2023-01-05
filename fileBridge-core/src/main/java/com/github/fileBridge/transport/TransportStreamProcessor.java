package com.github.fileBridge.transport;

import com.github.fileBridge.Looper;
import com.github.fileBridge.common.logger.GlobalLogger;
import com.github.fileBridge.common.Event;
import com.github.fileBridge.common.proto.EventOuterClass;
import com.github.fileBridge.event.EventLoop;
import com.github.fileBridge.handler.WaterMarkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * @author ZhiCheng
 * @date 2022/12/24 23:01
 */
public class TransportStreamProcessor extends AbstractTransportProcessor {

    private final Looper eventTransLooper;

    private final EventLoop eventLoop;

    private final int blockSize = Integer.parseInt(System.getProperty("blockSize", "10"));

    private final BlockingQueue<Event> retryQueue;
    private final int timeoutMillis;

    public TransportStreamProcessor(int lowWaterMark, int highWaterMark, int timeoutMillis, Loadbalancer loadbalancer,
                                    EventLoop eventLoop) {
        super(lowWaterMark, highWaterMark, loadbalancer);
        this.timeoutMillis = timeoutMillis;
        this.eventTransLooper = new Looper(this::loop, 1);
        this.eventLoop = eventLoop;
        this.retryQueue = new ArrayBlockingQueue<>(blockSize);
    }

    private void loop() throws InterruptedException {
        var client = loadbalancer.next();
        if (client == null) {
            return;
        }
        var waitControl = new CountDownLatch(1);
        var needRetry = new AtomicBoolean();
        var streamObserver = client.pushAsync(context -> {
            if (context.completed) {
                needRetry.set(false);
                waitControl.countDown();
                return;
            }
            if (context.throwable != null) {
                needRetry.set(true);
                loadbalancer.reportUnHealthy(client);
                GlobalLogger.getLogger().error("push error.", context.throwable);
                waitControl.countDown();
            }
        });
        var block = block();
        if (block.size() == 0) {
            return;
        }
        for (var ev : block) {
            if (needRetry.get()) {
                break;
            }
            streamObserver.onNext(EventOuterClass.Event.newBuilder()
                    .setOutput(ev.output()).setAbsPath(ev.absPath())
                    .setContent(ev.content()).putAllStruct(ev.mapping())
                    .setId(ev.id())
                    .build());
        }
        streamObserver.onCompleted();
        if (!waitControl.await(this.timeoutMillis, TimeUnit.MILLISECONDS)) {
            loadbalancer.reportUnHealthy(client);
            needRetry.set(true);
            GlobalLogger.getLogger().warn("request timout then,limit is " + timeoutMillis + " milliseconds");
        }
        if (needRetry.get()) {
            //等1秒重试,防止频繁的重试
            LockSupport.parkNanos(this, TimeUnit.SECONDS.toNanos(1));
            try {
                retryQueue.addAll(block);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            return;
        }
        //如果推送成功进行offset提交
        eventLoop.getOffsetRecorder().commitOffset(block.get(block.size() - 1));
    }

    private List<Event> block() throws InterruptedException {
        var block = new ArrayList<Event>();
        //优先执行重试队列
        if (retryQueue.size() == 0) {
            //eventbuffer中剩余的数量不够一个block时，会有最多300毫秒的延迟
            //设置这个延迟的原因是 如果不够一个block则说明此时日志生成速率低，这点延迟没有影响，同时减少空闲时期的cpu占用
            Event event;
            while (null != (event = poll(300, TimeUnit.MILLISECONDS))) {
                block.add(event);
                if (block.size() == blockSize) {
                    break;
                }
            }
            return block;
        } else {
            int size = retryQueue.size();
            for (int i = 0; i < size; i++) {
                Event polledEvent = retryQueue.poll();
                if (polledEvent == null) {
                    continue;
                }
                block.add(polledEvent);
            }
        }
        return block;
    }

    @Override
    public WaterMarkStatus waterMarkStatus() {
        int size = this.eventBuffer.size() + retryQueue.size();
        if (size < lowWaterMark) {
            return WaterMarkStatus.NORMAL;
        }
        if (size < highWaterMark) {
            return WaterMarkStatus.WARRING;
        }
        return WaterMarkStatus.OVERFLOW;
    }

    @Override
    public void start() {
        eventTransLooper.start();
    }

    @Override
    public void shutdown() {
        eventTransLooper.shutdown();
    }
}
