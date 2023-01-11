package com.github.fileBridge.common.exception;

/**
 * @author ZhiCheng
 * @date 2022/11/3 16:01
 */
public class ShutdownSignal extends RuntimeException{
    public ShutdownSignal() {
        super();
    }

    public ShutdownSignal(String message) {
        super(message);
    }

    public ShutdownSignal(String message, Throwable cause) {
        super(message, cause);
    }

    public ShutdownSignal(Throwable cause) {
        super(cause);
    }

    protected ShutdownSignal(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
