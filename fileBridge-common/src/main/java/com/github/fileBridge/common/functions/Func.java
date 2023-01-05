package com.github.fileBridge.common.functions;

/**
 * @author ZhiCheng
 * @date 2022/11/7 14:49
 */
@FunctionalInterface
public interface Func<R> {
    R invoke();
}

