package com.github.fileBridge.event;


import com.github.fileBridge.BootLoader;
import com.github.fileBridge.common.Shutdown;
import com.github.fileBridge.common.exception.ShutdownSignal;
import com.github.fileBridge.common.functions.Hook;
import com.github.fileBridge.common.logger.GlobalLogger;
import com.github.fileBridge.common.Event;
import com.github.fileBridge.common.utils.HashUtil;
import com.github.fileBridge.handler.EventHandler;
import com.github.fileBridge.OffsetRepository;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class EventLoop implements Shutdown {

    private static final AtomicInteger eventTypeIds = new AtomicInteger(0);

    protected final ByteBuf loopBuffer;

    private final List<EventHandler> eventHandlers = new CopyOnWriteArrayList<>();

    private final List<Hook> shutdownHooks = new ArrayList<>();

    private volatile boolean isShutdown = false;

    private final File file;

    private volatile boolean isStart = false;

    private final EventLoopExecutor.EventTask task;

    private final String output;

    private final EventLoopExecutor eventLoopExecutor;

    private final BootLoader bootLoader;

    private final OffsetRepository offsetRepository;

    protected final Selector selector;

    private long lastUpdateTime;

    public final String fileHash;

    public EventLoop(File file, EventLoopExecutor eventLoopExecutor,
                     String output, String readStrategy,
                     BootLoader bootLoader, EventHandler... handlers) throws IOException, NoSuchFieldException, IllegalAccessException {
        this.file = file;
        this.fileHash = HashUtil.logHash(file);
        this.lastUpdateTime = file.lastModified();
        this.eventLoopExecutor = eventLoopExecutor;
        this.bootLoader = bootLoader;
        this.offsetRepository = new OffsetRepository(file, this, readStrategy);

        this.selector = new Selector(file, offsetRepository.readOffset());
        this.loopBuffer = Unpooled.buffer();
        this.eventHandlers.addAll(List.of(handlers));
        this.task = new EventLoopExecutor.EventTask("EventLoop " + file.getName() + ":" + eventTypeIds.getAndIncrement(), this::runTask);
        this.output = output;
        registerShutdownHooks(() -> {
            try {
                selector.close();
            } catch (IOException e) {
                GlobalLogger.getLogger().error("selector error", e);
            }
        });
    }

    public void start() {
        if (!isStart) {
            eventLoopExecutor.exec(this.task);
            isStart = true;
        }
    }

    public void suspendNext() {
        eventLoopExecutor.suspend(this.task);
    }

    public void suspendNext(int milliSec) {
        eventLoopExecutor.suspend(this.task, milliSec);
    }

    public void resume() {
        eventLoopExecutor.recover(this.task);
    }


    public void runTask() {
        if (isShutdown()) {
            afterShutdown();
            GlobalLogger.getLogger().info("eventLoop was closed,fileHash is " + fileHash);
            throw new ShutdownSignal("eventLoop of " + fileHash + " is interrupted");
        }
        try {
            List<Selector.Line> lines = selectLines();
            //没有找到一行完整的日志而且已经到达文件末尾，此时休眠100毫秒
            if (lines.isEmpty() && isEOF()) {
                eventLoopExecutor.suspend(this.task, 100);
                return;
            }
            if (!lines.isEmpty()) {
                lastUpdateTime = System.currentTimeMillis();
            }
            handleLines(lines);
        } catch (IOException e) {
            shutdown();
            throw new ShutdownSignal("selector error so shutdown error is", e);
        }
    }

    public List<Selector.Line> selectLines() throws IOException {
        return selector.selectLine(loopBuffer);
    }


    public void handleLines(List<Selector.Line> lines) {
        try {
            for (Selector.Line line : lines) {
                executePipe(line);
            }
        } catch (IOException e) {
            shutdown();
            throw new ShutdownSignal("eventLoop of " + fileHash + " is shutdown", e);
        }
    }


    private void executePipe(Selector.Line line) throws IOException {
        Event event = new Event(
                line.content(),
                new HashMap<>(),
                output, line.offset(),
                HashUtil.MD5(this.fileHash + line.content() + line.offset())
        );
        new EventHandlerPipeline(eventHandlers).fireNext(event);
    }

    public void registerShutdownHooks(Hook... hooks) {
        this.shutdownHooks.addAll(List.of(hooks));
    }

    protected void afterShutdown() {
        if (isShutdown()) {
            try {
                this.shutdownHooks.forEach(Hook::hook);
            } catch (Exception e) {

            }
        }
    }

    public void registerEventHandler(EventHandler eventHandler) {
        eventHandlers.add(eventHandler);
    }


    public boolean isEOF() {
        return selector.isEOF();
    }

    public OffsetRepository getOffsetRecorder() {
        return offsetRepository;
    }


    @Override
    public void shutdown() {
        this.isShutdown = true;
    }

    public long fileSize() throws IOException {
        return selector.fileSize();
    }

    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public boolean isShutdown() {
        //文件不存在，说明eventLoop处于无效状态，此时关闭
        return (!file.exists() && isEOF()) || isShutdown;
    }

    public String getOutput() {
        return output;
    }

    public BootLoader getBootLoader() {
        return bootLoader;
    }
}

