package com.dinghz.tcpproxy.tcp.core;

import com.dinghz.tcpproxy.tcp.domain.TcpConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate;

    public TcpServerInitializer(TcpConfig tcpConfig, RestTemplate restTemplate) {
        this.tcpConfig = tcpConfig;
        this.restTemplate = restTemplate;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new TcpServerHandler(tcpConfig, restTemplate));
    }

}
