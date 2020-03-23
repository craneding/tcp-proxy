package com.dinghz.tcpproxy.nat.client.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * NatRealClientInitializer
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class NatRealClientInitializer extends ChannelInitializer<SocketChannel> {

    private final String id;
    private final ChannelHandlerContext natProxyClientContext;

    public NatRealClientInitializer(String id, ChannelHandlerContext natProxyClientContext) {
        this.id = id;
        this.natProxyClientContext = natProxyClientContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new NatRealClientHandler(id, natProxyClientContext));
    }

}
