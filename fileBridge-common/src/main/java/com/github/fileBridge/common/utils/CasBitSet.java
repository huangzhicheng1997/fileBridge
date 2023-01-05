package com.github.fileBridge.common.utils;

import com.github.fileBridge.common.functions.Func;
import com.github.fileBridge.common.functions.NoResultFunc;
import com.github.fileBridge.common.functions.NoResultOneArgFunc;
import com.github.fileBridge.common.functions.OneArgFunc;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于cas的bitset
 *
 * @author ZhiCheng
 * @date 2022/11/28 16:29
 */
public class CasBitSet {
    private final BitSet bitSet = new BitSet();

    private final AtomicInteger state = new AtomicInteger(0);

    private final OneArgFunc<Func<Boolean>, Boolean> casRead = (func) -> {
        while (true) {
            if (state.compareAndSet(0, 1)) break;
        }
        try {
            return func.invoke();
        } finally {
            state.set(0);
        }
    };

    private final NoResultOneArgFunc<NoResultFunc> casWrite = (func) -> {
        while (true) {
            if (state.compareAndSet(0, 1)) break;
        }
        try {
            func.invoke();
        } finally {
            state.set(0);
        }
    };

    public void set(int bitIndex) {
        casWrite.invoke(() -> bitSet.set(bitIndex));
    }

    public void set(int bitIndex, boolean value) {
        casWrite.invoke(() -> bitSet.set(bitIndex, value));
    }

    public boolean get(int bitIndex) {
        return casRead.invoke(() -> bitSet.get(bitIndex));
    }
}
