package com.dinghz.tcpproxy.nat.client.service;

import com.dinghz.tcpproxy.nat.core.cmd.CmdClose;
import com.dinghz.tcpproxy.nat.core.cmd.CmdNetData;
import com.dinghz.tcpproxy.nat.core.codec.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * NatRealClientHandler
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@Slf4j
@ChannelHandler.Sharable
public class NatRealClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final String id;
    private final ChannelHandlerContext natProxyClientContext;

    public NatRealClientHandler(String id, ChannelHandlerContext natProxyClientContext) {
        this.id = id;
        this.natProxyClientContext = natProxyClientContext;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        CmdClose cmdClose = new CmdClose();
        cmdClose.setId(id);
        natProxyClientContext.writeAndFlush(Codec.encode(ctx, cmdClose));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        int len = buf.readableBytes();

        byte[] data = new byte[len];
        buf.readBytes(data);

        CmdNetData cmdNetData = new CmdNetData();
        cmdNetData.setData(data);
        cmdNetData.setId(id);
        natProxyClientContext.writeAndFlush(Codec.encode(ctx, cmdNetData));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage(), cause);

        ctx.close();
    }
}
