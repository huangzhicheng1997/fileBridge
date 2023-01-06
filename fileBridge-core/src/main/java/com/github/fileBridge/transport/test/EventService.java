package com.github.fileBridge.transport.test;

import com.github.fileBridge.common.proto.EventOuterClass;
import com.github.fileBridge.common.proto.EventServiceGrpc;
import com.github.fileBridge.common.proto.ResOuterClass;
import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZhiCheng
 * @date 2022/11/7 11:49
 */
public class EventService extends EventServiceGrpc.EventServiceImplBase {
    AtomicInteger a=new AtomicInteger(0);
    @Override
    public StreamObserver<EventOuterClass.Event> pushAsync(StreamObserver<ResOuterClass.Res> responseObserver) {
        return new StreamObserver<>() {

            @Override
            public void onNext(EventOuterClass.Event event) {
                Map<String, String> structMap = event.getStructMap();
                if (structMap.isEmpty()){
                    String content = event.getContent();
                    System.out.print(content);
                    System.out.println(event.getId()+","+event.getAbsPath());
                }else {
                    System.out.print(structMap);
                    System.out.println(event.getId()+","+event.getAbsPath());
                }

                System.out.println(a.incrementAndGet());

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(
                        ResOuterClass.Res.newBuilder().setStatus(ResOuterClass.Res.Status.SUCCESS).build()
                );
                responseObserver.onCompleted();
            }


        };
    }


    @Override
    public void healthCheck(EventOuterClass.Event request, StreamObserver<ResOuterClass.Res> responseObserver) {
        responseObserver.onNext(ResOuterClass.Res.newBuilder().setStatus(ResOuterClass.Res.Status.SUCCESS).build());
        responseObserver.onCompleted();
    }
}
