package com.github.fileBridge.common.functions;

/**
 * @author ZhiCheng
 * @date 2022/11/28 16:38
 */
@FunctionalInterface
public interface OneArgFunc<T,R> {
    R invoke(T arg);
}
