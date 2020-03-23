package com.dinghz.tcpproxy.nat.server.service;

import com.dinghz.tcpproxy.nat.core.codec.CmdDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * NatRealMockServerInitializer
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class NatRealMockServerInitializer extends ChannelInitializer<SocketChannel> {

    private final String realHost;
    private final Integer realPort;
    private final ShareGroup shareGroup;

    public NatRealMockServerInitializer(String realHost, Integer realPort, ShareGroup shareGroup) {
        this.realHost = realHost;
        this.realPort = realPort;
        this.shareGroup = shareGroup;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new NatRealMockServerHandler(realHost, realPort, shareGroup));
    }

}
