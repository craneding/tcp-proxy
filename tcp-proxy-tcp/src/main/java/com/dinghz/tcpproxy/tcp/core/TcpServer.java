package com.dinghz.tcpproxy.tcp.core;

import com.dinghz.tcpproxy.tcp.domain.TcpConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * TcpServer
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@Slf4j
public class TcpServer {

    private final TcpConfig tcpConfig;

    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    public TcpServer(TcpConfig tcpConfig) {
        this.tcpConfig = tcpConfig;
    }

    public boolean start() {
        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024)
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new TcpServerInitializer(tcpConfig));

        try {
            Channel ch = b.bind(tcpConfig.getLocalPort()).sync().channel();

            log.info("启动监听 {} 成功", tcpConfig.getLocalPort());

            ch.closeFuture().addListener((ChannelFutureListener) future -> {
                log.info("关闭监听 {} {}", tcpConfig.getLocalPort(), !future.channel().isOpen() ? "成功" : "失败");
            });
        } catch (InterruptedException e) {
            log.info("启动监听 " + tcpConfig.getLocalPort() + " 失败", e);

            return false;
        }

        return true;
    }

    public void stop() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
    }
}
