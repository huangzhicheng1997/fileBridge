package com.github.fileBridge.common.exception;

/**
 * @author ZhiCheng
 * @date 2022/11/3 16:01
 */
public class ShutdownException extends RuntimeException{
    public ShutdownException() {
        super();
    }

    public ShutdownException(String message) {
        super(message);
    }

    public ShutdownException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShutdownException(Throwable cause) {
        super(cause);
    }

    protected ShutdownException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
