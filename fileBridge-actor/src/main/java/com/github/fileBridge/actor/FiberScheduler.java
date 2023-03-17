package com.github.fileBridge.actor;

import com.github.fileBridge.common.functions.SafeRunnable;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * @author ZhiCheng
 * @date 2023/3/16 15:43
 */
public class FiberScheduler extends AbstractActor<FiberScheduler.ChangeState> {

    public enum Instance {

        INSTANCE;

        final FiberScheduler fiberScheduler = new FiberScheduler();
    }

    public enum Status {
        READY, RUNNABLE, WAITING, TIMED_WAITING, TERMINAL;
    }

    public static class ChangeState {
        Fiber fiber;
        Status status;
        long timeout;
        SafeRunnable then;

        public ChangeState(Fiber fiber, Status status, long timeout) {
            this.fiber = fiber;
            this.status = status;
            this.timeout = timeout;
        }

        public ChangeState(Fiber fiber, Status status, long timeout, SafeRunnable then) {
            this.fiber = fiber;
            this.status = status;
            this.timeout = timeout;
            this.then = then;
        }
    }


    private final Map<Fiber, ChangeState> requestChannel = new ConcurrentHashMap<>();

    private final Channel<Fiber> readyQueue = new MpscBaseChannel<>();

    private final PriorityQueue<Fiber> timedWaitingQueue = new PriorityQueue<>((o1, o2) -> Long.compare(o2.timeout, o1.timeout));

    private final Set<Fiber> waitingSet = new HashSet<>();

    private final Executor executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    private final Thread boss = new Thread(this::loop);

    {
        boss.start();
    }

    @Override
    public String id() {
        return "FIBER_SCHEDULER";
    }


    @Override
    public void start() {
    }

    public static FiberScheduler getInstance() {
        return Instance.INSTANCE.fiberScheduler;
    }

    private void loop() {
        int times = 0;
        for (; ; ) {
            try {
                tryWakeup();
                Fiber fiber = readyQueue.recv();
                //busy 
                if (times >= 200 && readyQueue.isEmpty() && timedWaitingQueue.isEmpty()) {
                    times = 0;
                    LockSupport.parkNanos(this, TimeUnit.MILLISECONDS.toNanos(1));
                }
                if (null == fiber) {
                    continue;
                }
                if (requestChannel.containsKey(fiber)) {
                    ChangeState changeState = requestChannel.get(fiber);
                    fiber.status = changeState.status;
                    fiber.timeout = changeState.timeout;
                    if (changeState.then != null) fiber.runnable = changeState.then;
                    requestChannel.remove(fiber);
                }
                switch (fiber.status) {
                    case READY -> exec(fiber);
                    case RUNNABLE -> throw new IllegalStateException();
                    case TIMED_WAITING -> timedWaitingQueue.offer(fiber);
                    case WAITING -> waitingSet.add(fiber);
                    case TERMINAL -> {
                    }
                }
            } finally {
                times++;
            }
        }
    }

    @Override
    public void recv(ChangeState m) {
        requestChannel.put(m.fiber, m);
    }

    public void submit(Fiber fiber) {
        readyQueue.send(fiber);
    }

    private void tryWakeup() {
        while (!timedWaitingQueue.isEmpty()) {
            Fiber waitingFiber = timedWaitingQueue.peek();
            if (waitingFiber.status != Status.TIMED_WAITING) {
                throw new IllegalStateException();
            }
            if (waitingFiber.timeout > System.currentTimeMillis()) {
                break;
            }
            timedWaitingQueue.poll();
            waitingFiber.status = Status.READY;
            readyQueue.send(waitingFiber);
        }

        waitingSet.removeIf(fiber -> {
            ChangeState changeState = requestChannel.get(fiber);
            if (null == changeState) {
                return false;
            }
            if (changeState.status == Status.READY) {
                fiber.status = Status.READY;
                readyQueue.send(fiber);
                return true;
            }
            return false;
        });
    }

    private void exec(Fiber fiber) {
        fiber.status = Status.RUNNABLE;
        executor.execute(() -> {
            try {
                fiber.runnable.run();
            } finally {
                fiber.status = Status.READY;
                readyQueue.send(fiber);
            }
        });
    }


    public static class Fiber {
        private SafeRunnable runnable;
        private Status status;
        private long timeout;
        private final Map<Object, Object> fiberContext = new ConcurrentHashMap<>();

        public Fiber() {
            this.runnable = this::noop;
            this.timeout = Long.MAX_VALUE;
            this.status = Status.READY;
        }

        public Fiber(SafeRunnable runnable) {
            this.runnable = runnable;
            this.timeout = Long.MAX_VALUE;
            this.status = Status.READY;
        }


        public void setRunnable(SafeRunnable runnable) {
            this.runnable = runnable;
        }

        private void noop() {

        }

        public Object attr(Object key) {
            return fiberContext.get(key);
        }

        public void set(Object key, Object value) {
            fiberContext.put(key, value);
        }

        public void suspend() {
            Instance.INSTANCE.fiberScheduler.recv(new ChangeState(this, Status.WAITING, Long.MAX_VALUE));
        }

        public void suspendThen(SafeRunnable then) {
            Instance.INSTANCE.fiberScheduler.recv(new ChangeState(this, Status.WAITING, Long.MAX_VALUE, then));
        }

        public void suspend(long timeout) {
            Instance.INSTANCE.fiberScheduler.recv(new ChangeState(this, Status.TIMED_WAITING, System.currentTimeMillis() + timeout));
        }

        public void resume() {
            Instance.INSTANCE.fiberScheduler.recv(new ChangeState(this, Status.READY, Long.MAX_VALUE));
        }

        public void stop() {
            Instance.INSTANCE.fiberScheduler.recv(new ChangeState(this, Status.TERMINAL, Long.MAX_VALUE));
        }

    }

    public static void main(String[] args) throws InterruptedException {
        FiberScheduler instance = FiberScheduler.getInstance();
        Fiber fiber = new Fiber();

        var run3 = (SafeRunnable) () -> {
            System.out.println("3");
        };

        var run2 = (SafeRunnable) () -> {
            System.out.println("2");
            fiber.suspendThen(run3);
        };

        var run1 = (SafeRunnable) () -> {
            System.out.println("async");
            fiber.suspendThen(run2);
        };


        fiber.setRunnable(run1);

        instance.submit(fiber);
        while (true) {
            Thread.sleep(1000);
            if (fiber.status == Status.WAITING) {
                fiber.resume();
            }
        }
    }
}
