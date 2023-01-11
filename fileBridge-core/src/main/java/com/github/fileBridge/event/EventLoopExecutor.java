package com.github.fileBridge.event;

import com.github.fileBridge.common.Shutdown;
import com.github.fileBridge.common.exception.ShutdownSignal;
import com.github.fileBridge.common.logger.GlobalLogger;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.internal.shaded.org.jctools.queues.MpscUnboundedArrayQueue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

/**
 * eventLoop执行器
 *
 * @author ZhiCheng
 * @date 2022/9/28 18:00
 */
public class EventLoopExecutor implements Shutdown {

    private final MpscUnboundedArrayQueue<EventTask> readyQueue = new MpscUnboundedArrayQueue<>(1 << 7);

    /*
     * 复用netty的eventLoop，来实现线程+本地队列的非阻塞模式
     */
    private final DefaultEventExecutorGroup eventExecutorGroup;

    private final MpscUnboundedArrayQueue<EventOp> eventOpQueue = new MpscUnboundedArrayQueue<>(16);

    private final Thread scheduler;

    private volatile int schedulerState = 0;
    /*
     * 等待队列，类似于IO等待队列，用于存放被暂定的任务，等待被唤醒
     */
    private final List<EventTask> waitingQueue = new ArrayList<>();

    public EventLoopExecutor(int numberOfThreads) {
        eventExecutorGroup = new DefaultEventExecutorGroup(numberOfThreads);
        scheduler = new Thread(this::schedule);
        schedulerState = 1;
        scheduler.start();
    }

    public EventLoopExecutor(int numberOfThreads, ThreadFactory threadFactory) {
        eventExecutorGroup = new DefaultEventExecutorGroup(numberOfThreads, threadFactory);
        scheduler = new Thread(this::schedule);
        schedulerState = 1;
        scheduler.start();
    }


    private void schedule() {
        while (schedulerState == 1) {
            var loopTimes = 10;
            for (int waiting = loopTimes; waiting > 0; waiting--) {
                //处理必要的动作，例如唤醒、休眠
                EventOp eventOp = eventOpQueue.poll();
                suspend(eventOp);
                recover(eventOp);
                //处理任务
                EventTask eventTask = readyQueue.poll();
                if (eventTask == null) {
                    continue;
                }
                waiting = loopTimes;
                /*
                 * READY          ->  RUNNING
                 * RUNNING        ->  READY
                 * RUNNING        ->  WAITING
                 * RUNNING        ->  TIMED_WAITING
                 * WAITING        ->  READY
                 * TIMED_WAITING  ->  READY
                 */
                switch (eventTask.status) {
                    case READY, RUNNING -> {
                        commitTask(eventTask);
                    }
                    case WAITING, TIMED_WAITING -> {
                        if (!waitingQueue.contains(eventTask)) {
                            waitingQueue.add(eventTask);
                        }
                    }
                }
            }
            LockSupport.parkNanos(this, TimeUnit.MILLISECONDS.toNanos(1));
        }

    }

    private void suspend(EventOp eventOp) {
        if (eventOp == null) {
            return;
        }
        switch (eventOp.eventOpType) {
            case SUSPEND -> {
                eventOp.eventTask.setStatus(EventStatus.WAITING);
            }
            case TIMED_SUSPEND -> {
                eventOp.eventTask.waitingTime = eventOp.waitingTime;
                eventOp.eventTask.setStatus(EventStatus.TIMED_WAITING);
            }
        }
    }

    private void recover(EventOp eventOp) {
        Iterator<EventTask> iterator = waitingQueue.iterator();
        EventTask recovering = eventOp == null || eventOp.eventOpType != EventOpType.RECOVER ? null : eventOp.eventTask;

        while (iterator.hasNext()) {
            EventTask eventTask = iterator.next();
            switch (eventTask.getStatus()) {
                case TIMED_WAITING -> {
                    if (eventTask.startWaitingTime + eventTask.waitingTime < System.currentTimeMillis()) {
                        iterator.remove();
                        eventTask.status=EventStatus.READY;
                        exec(eventTask);
                    }
                }
                case WAITING -> {
                    if (recovering != null && recovering.id.equals(eventTask.id)) {
                        iterator.remove();
                        eventTask.status=EventStatus.READY;
                        exec(eventTask);
                    }
                }
            }
        }
    }

    private void commitTask(EventTask eventTask) {
        eventExecutorGroup.execute(() -> {
            try {
                eventTask.setStatus(EventStatus.RUNNING);
                eventTask.runnable.run();
                //执行完的任务重新进入就绪状态，形成循环
                exec(eventTask);
            } catch (ShutdownSignal e) {
                GlobalLogger.getLogger().info("EventTaskShutdown taskId is " + eventTask.id, e);
            } catch (Exception e) {
                GlobalLogger.getLogger().error("eventLoop error", e);
            }
        });
    }


    public void exec(EventTask task) {
        readyQueue.offer(task);
    }


    @Override
    public void shutdown() {
        schedulerState = 0;
        eventExecutorGroup.shutdownGracefully();
    }


    public void suspend(EventTask task) {
        eventOpQueue.offer(EventOp.newWaiting(task));
    }

    public void suspend(EventTask task, long milliSec) {
        eventOpQueue.offer(EventOp.newTimedWaiting(task, milliSec));
    }

    public void recover(EventTask task) {
        eventOpQueue.offer(EventOp.newRecover(task));
    }


    public static final class EventTask {
        private final String id;
        private final Runnable runnable;
        private volatile EventStatus status;
        private volatile Long waitingTime;
        private volatile Long startWaitingTime;

        public EventTask(String id, Runnable runnable) {
            this.id = id;
            this.runnable = runnable;
            status = EventStatus.READY;
        }

        public EventTask(String id, Function<EventTask, Runnable> func) {
            this.id = id;
            status = EventStatus.READY;
            this.runnable = func.apply(this);
        }


        public void setStatus(EventStatus status) {
            this.status = status;
            if (status == EventStatus.WAITING || status == EventStatus.TIMED_WAITING) {
                startWaitingTime = System.currentTimeMillis();
            }
        }

        public String getId() {
            return id;
        }

        public EventStatus getStatus() {
            return status;
        }
    }

    static class EventOp {
        EventTask eventTask;
        EventOpType eventOpType;
        Long waitingTime;

        private EventOp(EventTask eventTask, EventOpType eventOpType) {
            this.eventTask = eventTask;
            this.eventOpType = eventOpType;
        }

        private EventOp(EventTask eventTask, EventOpType eventOpType, Long waitingTime) {
            this.eventTask = eventTask;
            this.eventOpType = eventOpType;
            this.waitingTime = waitingTime;
        }

        static EventOp newWaiting(EventTask eventTask) {
            return new EventOp(eventTask, EventOpType.SUSPEND);
        }

        static EventOp newTimedWaiting(EventTask eventTask, long milliSec) {
            return new EventOp(eventTask, EventOpType.TIMED_SUSPEND, milliSec);
        }

        static EventOp newRecover(EventTask eventTask) {
            return new EventOp(eventTask, EventOpType.RECOVER);
        }
    }

    enum EventOpType {
        RECOVER, SUSPEND, TIMED_SUSPEND
    }
}