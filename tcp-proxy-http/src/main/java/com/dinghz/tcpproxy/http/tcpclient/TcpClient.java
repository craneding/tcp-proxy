package com.dinghz.tcpproxy.http.tcpclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * TcpClient
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class TcpClient {
    final EventLoopGroup workerGroup = new NioEventLoopGroup();
    final Bootstrap bootstrap = new Bootstrap();

    static final TcpClient tcpClient = new TcpClient();

    private TcpClient() {
    }

    public static TcpClient instance() {
        return tcpClient;
    }

    public void start() {
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new TcpClientInitializer());
    }

    public ChannelFuture connect(String host, int port) throws InterruptedException {
        return bootstrap.connect(host, port).sync();
    }

    public void stop() {
        workerGroup.shutdownGracefully();
    }

}
