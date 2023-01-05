package com.github.fileBridge.common.exception;

/**
 * @author ZhiCheng
 * @date 2022/12/19 16:08
 */
public class IllegalConfigException extends RuntimeException{
    public IllegalConfigException() {
        super();
    }

    public IllegalConfigException(String message) {
        super(message);
    }

    public IllegalConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalConfigException(Throwable cause) {
        super(cause);
    }

    protected IllegalConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
