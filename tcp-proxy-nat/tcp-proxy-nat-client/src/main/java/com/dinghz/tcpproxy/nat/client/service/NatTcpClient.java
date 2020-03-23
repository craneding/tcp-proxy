package com.dinghz.tcpproxy.nat.client.service;

import com.dinghz.tcpproxy.nat.core.cmd.CmdNetData;
import com.dinghz.tcpproxy.nat.core.codec.Codec;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

/**
 * NatProxyClient
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@Slf4j
public class NatTcpClient implements ChannelFutureListener {

    private EventLoopGroup workerGroup = null;
    private Bootstrap bootstrap = null;

    private final String host;
    private final Integer port;
    private final ChannelHandler handler;

    private Channel channel = null;

    public NatTcpClient(String host, Integer port, ChannelHandler handler) {
        this.host = host;
        this.port = port;
        this.handler = handler;
    }

    public void start() {
        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(handler);
    }

    public void connect() throws InterruptedException, ConnectException {
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        ChannelFuture future = bootstrap.connect(host, port).sync();
        channel = future.channel();
        if (channel == null) {
            throw new ConnectException("connect " + host + " " + port + " timeout");
        }
        if (!channel.isOpen()) {
            throw new ConnectException("connect " + host + " " + port + " error");
        }
        log.info("[连接成功][{}]", channel);
    }

    public void asyncConnect() {
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        ChannelFuture future = bootstrap.connect(host, port);
        future.addListener(this);
    }

    public void send(CmdNetData netData) throws ConnectException {
        if (!channel.isOpen()) {
            throw new ConnectException("not connect " + host + " " + port);
        }

        channel.writeAndFlush(Codec.encode(channel, netData));
    }

    public void send(ByteBuf buf) {
        channel.writeAndFlush(buf);
    }

    public void send(byte[] bytes) {
        ByteBuf buf = channel.alloc().buffer(bytes.length);
        buf.writeBytes(bytes);
        channel.writeAndFlush(buf);
    }

    @PreDestroy
    public void stop() {
        disconnect();

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
    }

    public void disconnect() {
        if (channel != null) {
            channel.attr(AttributeKey.valueOf("NoReConnect")).set(true);
            channel.close();
            channel = null;
        }
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            log.warn("[NatProxyClient][连接失败][" + host + ":" + port + "][10秒后重连]");

            final EventLoop loop = future.channel().eventLoop();
            loop.schedule(() -> asyncConnect(), 10L, TimeUnit.SECONDS);
        } else {
            log.info("[NatProxyClient][连接成功][" + host + ":" + port + "]");

            channel = future.channel();
        }
    }

    public Channel getChannel() {
        return channel;
    }
}
