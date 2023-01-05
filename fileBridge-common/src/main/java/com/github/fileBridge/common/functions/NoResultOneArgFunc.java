package com.github.fileBridge.common.functions;

/**
 * @author ZhiCheng
 * @date 2022/11/28 16:44
 */
@FunctionalInterface
public interface NoResultOneArgFunc<T> {
    void invoke(T arg);
}
