package com.github.fileBridge;

import com.github.fileBridge.common.config.AgentConfig;
import com.github.fileBridge.common.config.OutputYml;
import com.github.fileBridge.common.exception.IllegalConfigException;
import com.github.fileBridge.common.logger.GlobalLogger;
import com.github.fileBridge.common.utils.FileUtil;
import com.github.fileBridge.common.utils.StringUtils;
import com.github.fileBridge.event.EventLoop;
import com.github.fileBridge.event.EventLoopExecutor;
import com.github.fileBridge.common.se.ScriptsAccessor;
import com.github.fileBridge.handler.CommitControlHandler;
import com.github.fileBridge.handler.EventTransHandler;
import com.github.fileBridge.handler.LuaScriptHandler;
import com.github.fileBridge.handler.MultiLinesMergeHandler;
import com.github.fileBridge.transport.*;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author ZhiCheng
 * @date 2022/10/31 15:46
 */
public class BootLoader {
    public AgentConfig agentConfig;
    public final List<EventLoop> eventLoops;
    public final List<Runnable> quickCycle;
    public final List<Runnable> slowCycle;
    public final EventLoopExecutor eventLoopExecutor;
    //用于处理非阻塞延迟低的任务
    Timer quickTimer;
    //用于处理阻塞式的任务，有一定延时
    Timer slowTimer;
    ScriptsAccessor scriptsAccessor;
    GlobalTransports globalTransports;

    public BootLoader() throws IOException {
        agentConfig = AgentConfig.loadProperties();
        validateConfig(agentConfig);
        GlobalLogger.initSingleton(agentConfig);
        eventLoopExecutor = new EventLoopExecutor(Integer.parseInt(agentConfig.getSchedulerThreads()));
        quickTimer = new Timer(true);
        slowTimer = new Timer(false, 1000);
        eventLoops = new CopyOnWriteArrayList<>();
        quickCycle = new CopyOnWriteArrayList<>();
        slowCycle = new CopyOnWriteArrayList<>();
        scriptsAccessor = ScriptsAccessor.newScriptLoader();
        globalTransports = new GlobalTransports(agentConfig);
    }

    private void validateConfig(AgentConfig agentConfig) {
        agentConfig.getOutput().forEach((name, output) -> {
            String addresses = output.getAddresses();
            if (StringUtils.isBlank(addresses)) {
                throw new IllegalConfigException("output:" + name + " 'address' can not be empty!");
            }
            String dir = output.getDir();
            if (StringUtils.isBlank(dir)) {
                throw new IllegalConfigException("output:" + name + " 'dir' can not be empty!");
            }
            String invalidateTime = output.getInvalidateTime();
            if (StringUtils.isBlank(invalidateTime)) {
                throw new IllegalConfigException("output:" + name + " 'invalidateTime' can not be empty!");
            }
            String pattern = output.getPattern();
            if (StringUtils.isBlank(pattern)) {
                throw new IllegalConfigException("output:" + name + " 'pattern' can not be empty!");
            }
            String logPattern = output.getLogPattern();
            if (StringUtils.isBlank(logPattern)) {
                throw new IllegalConfigException("output:" + name + " 'logPattern' can not be empty!");
            }
            String waterMark = output.getWaterMark();
            String[] mark = waterMark.split("-");
            try {
                Integer.parseInt(mark[0]);
                Integer.parseInt(mark[1]);
            } catch (Exception e) {
                throw new IllegalArgumentException("waterMark illegal", e);
            }
            String transTimeout = output.getTransTimeout();
            try {
                Integer.parseInt(transTimeout);
            } catch (Exception e) {
                throw new IllegalArgumentException("transTimeout illegal", e);
            }

        });
    }


    public void start() {
        this.agentConfig.getOutput().forEach((outputName, outputYml) -> {
            String dir = outputYml.getDir();
            String regex = outputYml.getPattern();
            if (StringUtils.isEmpty(regex)) {
                throw new IllegalArgumentException(String.format("output %s Pattern is null", outputName));
            }

            Runnable task = () -> {
                try {
                    var pattern = Pattern.compile(regex);
                    //取出符合正则,以及未失效的文件
                    var list = FileUtil.newFile(dir).listFiles(file -> pattern.matcher(file.getName()).matches() && !isInvalidate(file.lastModified(), Long.parseLong(outputYml.getInvalidateTime())));
                    if (list == null || list.length == 0) {
                        return;
                    }
                    for (File file : list) {
                        EventLoop exist = findEventLoop(file, outputName);
                        if (null == exist) {
                            EventLoop eventLoop = createEventLoop(file, outputYml, outputName);
                            eventLoops.add(eventLoop);
                            registerEventHandlers(eventLoop, outputYml);
                            eventLoop.registerShutdownHooks(() -> {
                                eventLoops.remove(eventLoop);
                            });
                            eventLoop.start();
                            GlobalLogger.getLogger().info("eventLoop started file is :" + eventLoop.getFileAbs());
                        } else {
                            //长时间未更新的文件所绑定的eventLoop将被失效
                            boolean invalidate = isInvalidate(exist.getLastUpdateTime(), Long.parseLong(outputYml.getInvalidateTime()));
                            if (invalidate) {
                                GlobalLogger.getLogger().info("eventLoop will shutdown because file is invalidate,lastUpdateTime is " + new Date(exist.getLastUpdateTime()));
                                exist.shutdown();
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            task.run();
            this.quickCycle.add(task);
        });
        this.quickTimer.startServerCycle();
        this.slowTimer.startServerCycle();
    }


    private EventLoop createEventLoop(File file, OutputYml outputYml, String outputName) throws IOException, NoSuchFieldException, IllegalAccessException {
        String logPattern = outputYml.getLogPattern();
        if (StringUtils.isEmpty(logPattern)) {
            throw new IllegalArgumentException("logPattern is null.");
        }
        OffsetRecorder offsetRecorder = new OffsetRecorder(file, outputYml.getReadStrategy());
        return new EventLoop(file, eventLoopExecutor, outputName, offsetRecorder, this);
    }


    private void registerEventHandlers(EventLoop eventLoop, OutputYml outputYml) {
        //注册handler
        //head
        eventLoop.registerEventHandler(new MultiLinesMergeHandler(outputYml.getLogPattern(), eventLoop));
        //-----------自定义-----------
        eventLoop.registerEventHandler(new LuaScriptHandler(scriptsAccessor, outputYml));
        eventLoop.registerEventHandler(new EventTransHandler(transportProcessor(eventLoop, outputYml), eventLoop));
        //tail
        eventLoop.registerEventHandler(new CommitControlHandler(eventLoop.getOffsetRecorder()));

    }

    private TransportProcessor transportProcessor(EventLoop eventLoop, OutputYml outputYml) {
        TransportTableInfo transportTableInfo = globalTransports.attach(outputYml);
        Loadbalancer loadbalancer = new Loadbalancer(transportTableInfo, quickCycle::add);
        slowCycle.add(loadbalancer::tryRecover);
        String[] waterMark = outputYml.getWaterMark().split("-");
        TransportProcessor transportProcessor = new TransportStreamProcessor(
                Integer.parseInt(waterMark[0]),
                Integer.parseInt(waterMark[1]),
                Integer.parseInt(outputYml.getTransTimeout()),
                loadbalancer,
                eventLoop
        );
        eventLoop.registerShutdownHooks(() -> {
            transportProcessor.shutdown();
            eventLoop.getOffsetRecorder().release();
        });
        transportProcessor.start();
        return transportProcessor;
    }

    private boolean isInvalidate(long lastUpdateTime, long invalidateSec) {
        return System.currentTimeMillis() - lastUpdateTime >= TimeUnit.SECONDS.toMillis(invalidateSec);
    }

    private EventLoop findEventLoop(File file, String outputName) {
        for (EventLoop eventLoop : eventLoops) {
            // 文件和输出都一样才代表相同的eventLoop
            if (eventLoop.getFileAbs().equals(file.getAbsolutePath())
                    && eventLoop.getOutput().equals(outputName)) {
                return eventLoop;
            }
        }
        return null;
    }


    private class Timer {

        private HashedWheelTimer timer;
        volatile boolean isStopping = false;
        volatile boolean isStopped = false;
        private final boolean isQuick;
        private final int frequency;

        public Timer(boolean isQuick) {
            this(isQuick, 300);
        }

        public Timer(boolean isQuick, int frequency) {
            this.isQuick = isQuick;
            this.frequency = frequency;
        }

        private void startServerCycle() {
            this.timer = new HashedWheelTimer();

            TimeUnit timeUnit = TimeUnit.MILLISECONDS;
            this.timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) {
                    runAll();
                    if (!isStopping) {
                        timeout.timer().newTimeout(this, frequency, timeUnit);
                    } else {
                        isStopped = true;
                    }
                }
            }, 100, timeUnit);
            this.timer.start();
        }

        private void runAll() {
            for (Runnable runnable : isQuick ? BootLoader.this.quickCycle : BootLoader.this.slowCycle) {
                try {
                    runnable.run();
                } catch (Exception ignore) {

                }
            }
        }

        public void shutdown() {
            isStopping = true;
            while (!isStopped) {
                Thread.onSpinWait();
            }
            timer.stop();
        }
    }
}
