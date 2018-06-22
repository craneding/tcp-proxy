package com.dinghz.tcpproxy.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TcpServer
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class TcpServer {
    static final Logger logger = LoggerFactory.getLogger(TcpServer.class);

    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    static final TcpServer tcpServer = new TcpServer();

    private TcpServer() {
    }

    public static TcpServer instance() {
        return tcpServer;
    }

    public void start() throws Exception {
        boolean good = false;

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024)
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new TcpServerInitializer());

            Channel ch = b.bind(TcpConfig.LOCAL_PORT).sync().channel();

            logger.info("监听 {} 成功", TcpConfig.LOCAL_PORT);

            good = true;

            ch.closeFuture().sync();
        } finally {
            if (!good) {
                logger.error("监听 {} 失败", TcpConfig.LOCAL_PORT);
            }

            stop();
        }
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
