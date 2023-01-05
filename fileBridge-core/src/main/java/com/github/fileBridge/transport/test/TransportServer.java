package com.github.fileBridge.transport.test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * @author ZhiCheng
 * @date 2022/11/7 14:13
 */
public class TransportServer {

    private final Server grpc;

    public TransportServer(int port) {
        this.grpc = ServerBuilder.forPort(port).addService(new EventService())
                .executor(Executors.newFixedThreadPool(4))
                .build();

    }


    public void start() throws IOException {
        grpc.start();
    }

    public void shutdown() {
        grpc.shutdown();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Logger root = (Logger) LoggerFactory.getLogger("ROOT");
        root.setLevel(Level.OFF);
        TransportServer transportServer = new TransportServer(9999);
        transportServer.start();
        Thread.sleep(100000);
    }


}
