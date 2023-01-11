package com.github.fileBridge.common.functions;

import com.github.fileBridge.common.exception.ShutdownSignal;
import com.github.fileBridge.common.logger.GlobalLogger;

public interface SafeRunnable extends Runnable {

    @Override
    default void run() {
        try {
            safeRun();
        } catch (InterruptedException | ShutdownSignal exception) {
            throw new ShutdownSignal();
        } catch (Throwable t) {
            GlobalLogger.getLogger().error("error", t);
        }
    }

    void safeRun() throws Throwable;
}
