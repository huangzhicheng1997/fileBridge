package com.github.fileBridge.remoting;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.util.Locale;

/**
 * @author ZhiCheng
 * @date 2023/3/17 16:20
 */
public class Server {
    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup boss;
    private final EventLoopGroup worker;
    private final DefaultEventExecutorGroup business;

    public Server() {
        serverBootstrap = new ServerBootstrap();
        boss = new NioEventLoopGroup(1);
        worker = new NioEventLoopGroup(4);
        business = new DefaultEventExecutorGroup(8);
    }

    public void start() {
        serverBootstrap.group(boss, worker).channelFactory(() -> {
            String osName = System.getProperty("os.name");
            if (osName.toLowerCase(Locale.ROOT).contains("linux")) {
                return new EpollServerSocketChannel();
            } else if (osName.toLowerCase(Locale.ROOT).contains("mac")) {
                return new KQueueServerSocketChannel();
            } else {
                return new NioServerSocketChannel();
            }
        }).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(business, new ChannelDecoder(65535, 0, 4))
                        .addLast(business, new ChannelEncoder())
                        .addLast(business, new IdleStateHandler(0,0,10))
                        .addLast(business,new SimpeChan)
                ;
            }
        });
    }
}
