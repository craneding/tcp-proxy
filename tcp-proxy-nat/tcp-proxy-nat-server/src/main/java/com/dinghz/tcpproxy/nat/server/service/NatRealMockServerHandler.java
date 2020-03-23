package com.dinghz.tcpproxy.nat.server.service;

import com.dinghz.tcpproxy.nat.core.cmd.*;
import com.dinghz.tcpproxy.nat.core.codec.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * NatRealMockServerHandler
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@Slf4j
@ChannelHandler.Sharable
public class NatRealMockServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final String realHost;
    private final Integer realPort;
    private final ShareGroup shareGroup;

    public NatRealMockServerHandler(String realHost, Integer realPort, ShareGroup shareGroup) {
        this.realHost = realHost;
        this.realPort = realPort;
        this.shareGroup = shareGroup;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String id = UUID.randomUUID().toString().replace("-", "");
        ctx.channel().attr(AttributeKey.valueOf("id")).set(id);
        shareGroup.putClientContext(id, ctx);

        // 发送连接信息到内网
        CmdConnect cmdConnect = new CmdConnect();
        cmdConnect.setRealHost(realHost);
        cmdConnect.setRealPort(realPort);
        cmdConnect.setId(id);
        shareGroup.sendToInnerNat(cmdConnect);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 发送关闭信息到内网
        CmdClose cmdClose = new CmdClose();
        cmdClose.setId((String) ctx.channel().attr(AttributeKey.valueOf("id")).get());
        shareGroup.sendToInnerNat(cmdClose);

        shareGroup.removeClientContext(cmdClose.getId());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        int len = buf.readableBytes();
        if (len > 0) {
            byte[] data = new byte[len];
            buf.readBytes(data);

            // 发送数据信息到内网
            CmdNetData cmdNetData = new CmdNetData();
            cmdNetData.setId((String) ctx.channel().attr(AttributeKey.valueOf("id")).get());
            cmdNetData.setData(data);
            shareGroup.sendToInnerNat(cmdNetData);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage(), cause);

        ctx.close();
    }
}
