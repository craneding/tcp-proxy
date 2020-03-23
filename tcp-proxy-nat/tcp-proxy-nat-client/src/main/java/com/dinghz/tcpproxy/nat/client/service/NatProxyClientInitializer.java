package com.dinghz.tcpproxy.nat.client.service;

import com.dinghz.tcpproxy.nat.core.codec.CmdDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * NatProxyClientInitializer
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class NatProxyClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new IdleStateHandler(45, 20, 0, TimeUnit.SECONDS));
        p.addLast(new CmdDecoder());
        p.addLast(new NatProxyClientHandler());
    }

}
