package com.dinghz.tcpproxy.nat.server.service;

import com.dinghz.tcpproxy.nat.core.codec.CmdDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * NatProxyServerInitializer
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class NatProxyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final ShareGroup shareGroup;

    public NatProxyServerInitializer(ShareGroup shareGroup) {
        this.shareGroup = shareGroup;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
        p.addLast(new CmdDecoder());
        p.addLast(new NatProxyServerHandler(shareGroup));
    }

}
