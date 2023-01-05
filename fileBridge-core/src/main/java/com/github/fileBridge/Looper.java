package com.github.fileBridge;

import com.github.fileBridge.common.functions.SafeRunnable;
import com.github.fileBridge.common.logger.GlobalLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class Looper implements Executor {

    private boolean isShutdown;
    private volatile boolean isStopped;
    private final SafeRunnable target;
    private int loopTimesPerMilli;
    private Thread thread;

    public Looper(SafeRunnable target, int loopTimesPerMilli) {
        this.target = target;
        this.loopTimesPerMilli = loopTimesPerMilli;
        try {
            //尝试获取虚拟线程 jdk19
            Method ofVirtualMethod = Thread.class.getMethod("ofVirtual");
            ofVirtualMethod.setAccessible(true);
            Object builder = ofVirtualMethod.invoke(Thread.class);
            Class<?> builderClass = builder.getClass();
            Method factoryMethod = builderClass.getMethod("factory");
            factoryMethod.setAccessible(true);
            Object factory = factoryMethod.invoke(builder);
            Method newThread = factory.getClass().getMethod("newThread", Runnable.class);
            newThread.setAccessible(true);
            thread = (Thread) newThread.invoke(factory, (Runnable) () -> {
                for (; ; ) {
                    Looper.this.run();
                }
            });
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            thread = new Thread(() -> {
                for (; ; ) {
                    Looper.this.run();
                }
            });
        }

    }

    public void run() {
        if (isShutdown) {
            isStopped = true;
            return;
        }
        try {
            int loop = loopTimesPerMilli();
            for (int i = 0; i < loop; i++) {
                target.run();
            }
            LockSupport.parkNanos(this, TimeUnit.MILLISECONDS.toNanos(1));
        } catch (Throwable throwable) {
            GlobalLogger.getLogger().error("error", throwable);
        }
    }


    @Override
    public void start() {
        isStopped = false;
        thread.start();
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        waitingStop();
    }

    private void waitingStop() {
        while (!isStopped) {
            Thread.onSpinWait();
        }
    }

    public int loopTimesPerMilli() {
        return this.loopTimesPerMilli;
    }

    public boolean isStopped() {
        return isStopped;
    }


    public void changeLoopTimes(int loopTimesPerMilli) {
        this.loopTimesPerMilli = loopTimesPerMilli;
    }
}
