package com.github.fileBridge.event;


import com.github.fileBridge.BootLoader;
import com.github.fileBridge.actor.FiberScheduler;
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

    private final FiberScheduler.Fiber fiber = new FiberScheduler.Fiber();

    private final String output;

    private final BootLoader bootLoader;

    private final OffsetRepository offsetRepository;

    protected final Selector selector;

    private long lastUpdateTime;

    public final String fileHash;

    public EventLoop(File file, String output, String readStrategy,
                     BootLoader bootLoader, EventHandler... handlers) throws IOException, NoSuchFieldException, IllegalAccessException {
        this.file = file;
        this.fileHash = HashUtil.logHash(file);
        this.lastUpdateTime = file.lastModified();
        this.bootLoader = bootLoader;
        this.offsetRepository = new OffsetRepository(file, this, readStrategy);
        this.selector = new Selector(file, offsetRepository.readOffset());
        this.loopBuffer = Unpooled.buffer();
        this.eventHandlers.addAll(List.of(handlers));
        this.fiber.setRunnable(this::runTask);
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
            FiberScheduler.getInstance().submit(fiber);
            isStart = true;
        }
    }

    public void suspendNext() {
         fiber.suspend();
    }

    public void suspendNext(int milliSec) {
        fiber.suspend(milliSec);
    }

    public void resume() {
        fiber.resume();
    }


    public void runTask() {
        if (isShutdown()) {
            afterShutdown();
            GlobalLogger.getLogger().info("eventLoop was closed,fileHash is " + fileHash);
            throw new ShutdownSignal("eventLoop of " + fileHash + " is interrupted");
        }
        try {
            List<Selector.Line> lines = selector.selectLine(loopBuffer);
            //没有找到一行完整的日志而且已经到达文件末尾，此时休眠100毫秒
            if (lines.isEmpty() && isEOF()) {
                fiber.suspend(100);
                return;
            }
            if (!lines.isEmpty()) {
                lastUpdateTime = System.currentTimeMillis();
            }
            for (Selector.Line line : lines) {
                Event event = new Event(
                        line.content(),
                        new HashMap<>(),
                        output, line.offset(),
                        HashUtil.MD5(this.fileHash + line.content() + line.offset())
                );
                new EventHandlerPipeline(eventHandlers).fireNext(event);
            }
        } catch (IOException e) {
            shutdown();
            throw new ShutdownSignal("selector error so shutdown error is", e);
        }
    }

    public void registerShutdownHooks(Hook... hooks) {
        this.shutdownHooks.addAll(List.of(hooks));
    }

    protected void afterShutdown() {
        if (isShutdown()) {
            try {
                this.shutdownHooks.forEach(Hook::hook);
            } catch (Exception e) {
                GlobalLogger.getLogger().error("error", e);
            }
        }
    }

    public void registerEventHandler(EventHandler eventHandler) {
        eventHandlers.add(eventHandler);
    }


    public boolean isEOF() {
        return selector.isEOF();
    }

    public OffsetRepository offsetRecorder() {
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

    public String output() {
        return output;
    }

    public BootLoader getBootLoader() {
        return bootLoader;
    }
}

