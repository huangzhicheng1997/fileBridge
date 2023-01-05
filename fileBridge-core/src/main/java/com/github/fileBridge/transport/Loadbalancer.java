package com.github.fileBridge.transport;

import com.github.fileBridge.common.functions.SafeRunnable;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author ZhiCheng
 * @date 2022/12/26 15:42
 */
public class Loadbalancer  {
    private static final int OP_TRY_RECOVER = 0;
    private static final int OP_REPORT_EXCEPTION = 1;
    private final TransportTableInfo transportTableInfo;

    public record OpContext<T>(int opcode, T attachment) {
    }

    private final BlockingQueue<Loadbalancer.OpContext<TransportClient>> opq = new ArrayBlockingQueue<>(128);

    public Loadbalancer(TransportTableInfo transportTableInfo, Consumer<SafeRunnable> schedulerProvider) {
        this.transportTableInfo = transportTableInfo;
        schedulerProvider.accept(this::loop);
    }

    public TransportClient next() {
        return this.transportTableInfo.next();
    }

    public void reportUnHealthy(TransportClient client) {
        opq.offer(new OpContext<>(OP_REPORT_EXCEPTION, client));
    }

    public void tryRecover() {
        opq.offer(new OpContext<>(OP_TRY_RECOVER, null));
    }


    private void loop() throws InterruptedException {
        Loadbalancer.OpContext<TransportClient> evOp = opq.poll(300, TimeUnit.MILLISECONDS);
        if (evOp == null) {
            return;
        }
        switch (evOp.opcode) {
            case OP_REPORT_EXCEPTION -> transportTableInfo.reportFailed(evOp.attachment);
            case OP_TRY_RECOVER -> transportTableInfo.recoverIfNecessary();
        }
    }
}
