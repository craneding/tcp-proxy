package com.dinghz.tcpproxy.tcp.core;

import com.dinghz.tcpproxy.tcp.domain.TcpConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * TcpServerInitializer
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class TcpServerInitializer extends ChannelInitializer<SocketChannel> {

    private final TcpConfig tcpConfig;

    public TcpServerInitializer(TcpConfig tcpConfig) {
        this.tcpConfig = tcpConfig;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new TcpServerHandler(tcpConfig));
    }

}
