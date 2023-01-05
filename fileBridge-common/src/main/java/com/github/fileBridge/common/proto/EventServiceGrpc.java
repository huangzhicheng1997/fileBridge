package com.github.fileBridge.common.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.51.1)",
    comments = "Source: EventService.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class EventServiceGrpc {

  private EventServiceGrpc() {}

  public static final String SERVICE_NAME = "EventService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.github.fileBridge.common.proto.EventOuterClass.Event,
      com.github.fileBridge.common.proto.ResOuterClass.Res> getPushAsyncMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "pushAsync",
      requestType = com.github.fileBridge.common.proto.EventOuterClass.Event.class,
      responseType = com.github.fileBridge.common.proto.ResOuterClass.Res.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<com.github.fileBridge.common.proto.EventOuterClass.Event,
      com.github.fileBridge.common.proto.ResOuterClass.Res> getPushAsyncMethod() {
    io.grpc.MethodDescriptor<com.github.fileBridge.common.proto.EventOuterClass.Event, com.github.fileBridge.common.proto.ResOuterClass.Res> getPushAsyncMethod;
    if ((getPushAsyncMethod = EventServiceGrpc.getPushAsyncMethod) == null) {
      synchronized (EventServiceGrpc.class) {
        if ((getPushAsyncMethod = EventServiceGrpc.getPushAsyncMethod) == null) {
          EventServiceGrpc.getPushAsyncMethod = getPushAsyncMethod =
              io.grpc.MethodDescriptor.<com.github.fileBridge.common.proto.EventOuterClass.Event, com.github.fileBridge.common.proto.ResOuterClass.Res>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "pushAsync"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.github.fileBridge.common.proto.EventOuterClass.Event.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.github.fileBridge.common.proto.ResOuterClass.Res.getDefaultInstance()))
              .setSchemaDescriptor(new EventServiceMethodDescriptorSupplier("pushAsync"))
              .build();
        }
      }
    }
    return getPushAsyncMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.github.fileBridge.common.proto.EventOuterClass.Event,
      com.github.fileBridge.common.proto.ResOuterClass.Res> getHealthCheckMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "healthCheck",
      requestType = com.github.fileBridge.common.proto.EventOuterClass.Event.class,
      responseType = com.github.fileBridge.common.proto.ResOuterClass.Res.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.github.fileBridge.common.proto.EventOuterClass.Event,
      com.github.fileBridge.common.proto.ResOuterClass.Res> getHealthCheckMethod() {
    io.grpc.MethodDescriptor<com.github.fileBridge.common.proto.EventOuterClass.Event, com.github.fileBridge.common.proto.ResOuterClass.Res> getHealthCheckMethod;
    if ((getHealthCheckMethod = EventServiceGrpc.getHealthCheckMethod) == null) {
      synchronized (EventServiceGrpc.class) {
        if ((getHealthCheckMethod = EventServiceGrpc.getHealthCheckMethod) == null) {
          EventServiceGrpc.getHealthCheckMethod = getHealthCheckMethod =
              io.grpc.MethodDescriptor.<com.github.fileBridge.common.proto.EventOuterClass.Event, com.github.fileBridge.common.proto.ResOuterClass.Res>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "healthCheck"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.github.fileBridge.common.proto.EventOuterClass.Event.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.github.fileBridge.common.proto.ResOuterClass.Res.getDefaultInstance()))
              .setSchemaDescriptor(new EventServiceMethodDescriptorSupplier("healthCheck"))
              .build();
        }
      }
    }
    return getHealthCheckMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static EventServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EventServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EventServiceStub>() {
        @java.lang.Override
        public EventServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EventServiceStub(channel, callOptions);
        }
      };
    return EventServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static EventServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EventServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EventServiceBlockingStub>() {
        @java.lang.Override
        public EventServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EventServiceBlockingStub(channel, callOptions);
        }
      };
    return EventServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static EventServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EventServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EventServiceFutureStub>() {
        @java.lang.Override
        public EventServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EventServiceFutureStub(channel, callOptions);
        }
      };
    return EventServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class EventServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<com.github.fileBridge.common.proto.EventOuterClass.Event> pushAsync(
        io.grpc.stub.StreamObserver<com.github.fileBridge.common.proto.ResOuterClass.Res> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getPushAsyncMethod(), responseObserver);
    }

    /**
     */
    public void healthCheck(com.github.fileBridge.common.proto.EventOuterClass.Event request,
        io.grpc.stub.StreamObserver<com.github.fileBridge.common.proto.ResOuterClass.Res> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getHealthCheckMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getPushAsyncMethod(),
            io.grpc.stub.ServerCalls.asyncClientStreamingCall(
              new MethodHandlers<
                com.github.fileBridge.common.proto.EventOuterClass.Event,
                com.github.fileBridge.common.proto.ResOuterClass.Res>(
                  this, METHODID_PUSH_ASYNC)))
          .addMethod(
            getHealthCheckMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                com.github.fileBridge.common.proto.EventOuterClass.Event,
                com.github.fileBridge.common.proto.ResOuterClass.Res>(
                  this, METHODID_HEALTH_CHECK)))
          .build();
    }
  }

  /**
   */
  public static final class EventServiceStub extends io.grpc.stub.AbstractAsyncStub<EventServiceStub> {
    private EventServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EventServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EventServiceStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.github.fileBridge.common.proto.EventOuterClass.Event> pushAsync(
        io.grpc.stub.StreamObserver<com.github.fileBridge.common.proto.ResOuterClass.Res> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncClientStreamingCall(
          getChannel().newCall(getPushAsyncMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void healthCheck(com.github.fileBridge.common.proto.EventOuterClass.Event request,
        io.grpc.stub.StreamObserver<com.github.fileBridge.common.proto.ResOuterClass.Res> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getHealthCheckMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class EventServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<EventServiceBlockingStub> {
    private EventServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EventServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EventServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.github.fileBridge.common.proto.ResOuterClass.Res healthCheck(com.github.fileBridge.common.proto.EventOuterClass.Event request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getHealthCheckMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class EventServiceFutureStub extends io.grpc.stub.AbstractFutureStub<EventServiceFutureStub> {
    private EventServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EventServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EventServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.github.fileBridge.common.proto.ResOuterClass.Res> healthCheck(
        com.github.fileBridge.common.proto.EventOuterClass.Event request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getHealthCheckMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_HEALTH_CHECK = 0;
  private static final int METHODID_PUSH_ASYNC = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final EventServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(EventServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_HEALTH_CHECK:
          serviceImpl.healthCheck((com.github.fileBridge.common.proto.EventOuterClass.Event) request,
              (io.grpc.stub.StreamObserver<com.github.fileBridge.common.proto.ResOuterClass.Res>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PUSH_ASYNC:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.pushAsync(
              (io.grpc.stub.StreamObserver<com.github.fileBridge.common.proto.ResOuterClass.Res>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class EventServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    EventServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.github.fileBridge.common.proto.EventServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("EventService");
    }
  }

  private static final class EventServiceFileDescriptorSupplier
      extends EventServiceBaseDescriptorSupplier {
    EventServiceFileDescriptorSupplier() {}
  }

  private static final class EventServiceMethodDescriptorSupplier
      extends EventServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    EventServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (EventServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new EventServiceFileDescriptorSupplier())
              .addMethod(getPushAsyncMethod())
              .addMethod(getHealthCheckMethod())
              .build();
        }
      }
    }
    return result;
  }
}