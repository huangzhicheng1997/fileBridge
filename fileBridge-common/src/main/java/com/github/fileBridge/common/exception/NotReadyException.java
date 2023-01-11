package com.github.fileBridge.common.exception;

/**
 * @author ZhiCheng
 * @date 2023/1/11 16:41
 */
public class NotReadyException extends RuntimeException{
    public NotReadyException() {
        super();
    }

    public NotReadyException(String message) {
        super(message);
    }

    public NotReadyException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotReadyException(Throwable cause) {
        super(cause);
    }

    protected NotReadyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
