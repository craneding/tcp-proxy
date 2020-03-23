package com.dinghz.tcpproxy.nat.server.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@Slf4j
public class Server {

    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final Integer port;
    private final ChannelHandler childHandler;

    public Server(Integer port, ChannelHandler childHandler) {
        this.port = port;
        this.childHandler = childHandler;
    }

    @PostConstruct
    public boolean start() {
        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024)
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(childHandler);

        try {
            Channel ch = b.bind(port).sync().channel();

            log.info("启动监听 {} 成功", port);

            ch.closeFuture().addListener((ChannelFutureListener) future -> {
                log.info("关闭监听 {} {}", port, !future.channel().isOpen() ? "成功" : "失败");
            });
        } catch (Exception e) {
            log.info("启动监听 " + port + " 失败", e);

            return false;
        }

        return true;
    }

    @PreDestroy
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
