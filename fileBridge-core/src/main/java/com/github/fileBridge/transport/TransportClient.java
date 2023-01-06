package com.github.fileBridge.transport;

import com.github.fileBridge.common.proto.EventOuterClass;
import com.github.fileBridge.common.proto.EventServiceGrpc;
import com.github.fileBridge.common.proto.ResOuterClass;
import io.grpc.*;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.stub.CallStreamObserver;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.*;

/**
 * @author ZhiCheng
 * @date 2022/11/7 14:30
 */
public class TransportClient {
    private final EventServiceGrpc.EventServiceBlockingStub blockingStub;
    private final EventServiceGrpc.EventServiceStub stub;
    private final ManagedChannel channel;
    public final String ip;
    public final int port;

    public TransportClient(String ip, int port, ExecutorService executorService) {
        NameResolverRegistry.getDefaultRegistry().register(new DnsNameResolverProvider());
        channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext()
                .executor(executorService).build();
        blockingStub = EventServiceGrpc.newBlockingStub(channel);
        stub = EventServiceGrpc.newStub(channel);
        this.ip = ip;
        this.port = port;
    }

    public boolean healthCheck() {
        ResOuterClass.Res res = this.blockingStub.healthCheck(EventOuterClass.Event.newBuilder().build());
        return res.getStatus() == ResOuterClass.Res.Status.SUCCESS;
    }

    public CallStreamObserver<EventOuterClass.Event> pushAsync(ObserverListener listener) {
        var streamObserver = new StreamObserver<ResOuterClass.Res>() {
            @Override
            public void onNext(ResOuterClass.Res value) {
                var observerContext = new ObserverContext();
                observerContext.res = value;
                observerContext.completed = true;
                listener.listen(observerContext);
            }

            @Override
            public void onError(Throwable t) {
                var ctx = new ObserverContext();
                ctx.throwable = t;
                listener.listen(ctx);
            }

            @Override
            public void onCompleted() {

            }
        };
        return (CallStreamObserver<EventOuterClass.Event>) stub.pushAsync(streamObserver);
    }


    public void shutdown() {
        channel.shutdown();
    }


}
