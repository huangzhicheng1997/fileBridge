package com.github.fileBridge.common.functions;

@FunctionalInterface
public interface NoResultTwoArgsFunc<P1, P2> {
    void invoke(P1 p1, P2 p2);
}
