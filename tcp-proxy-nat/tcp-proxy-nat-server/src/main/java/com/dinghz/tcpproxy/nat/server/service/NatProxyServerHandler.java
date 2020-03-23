package com.dinghz.tcpproxy.nat.server.service;

import com.dinghz.tcpproxy.nat.core.cmd.AbstractCmd;
import com.dinghz.tcpproxy.nat.core.cmd.CmdClose;
import com.dinghz.tcpproxy.nat.core.cmd.CmdHeartbeat;
import com.dinghz.tcpproxy.nat.core.cmd.CmdNetData;
import com.dinghz.tcpproxy.nat.core.codec.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * NatProxyServerHandler
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@Slf4j
@ChannelHandler.Sharable
public class NatProxyServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final AtomicInteger connections = new AtomicInteger();
    private final ShareGroup shareGroup;

    public NatProxyServerHandler(ShareGroup shareGroup) {
        this.shareGroup = shareGroup;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        int val = connections.incrementAndGet();
        if (val == 1) {
            super.channelActive(ctx);

            shareGroup.setNatProxyServerContext(ctx);
        } else {
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        connections.decrementAndGet();

        if (shareGroup.getNatProxyServerContext() == ctx) {
            shareGroup.setNatProxyServerContext(null);

            shareGroup.closeClients();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, ByteBuf buf) throws Exception {
        AbstractCmd cmd = Codec.decode(buf);

        if (cmd instanceof CmdNetData) {
            shareGroup.sendToClient((CmdNetData) cmd);
        } else if (cmd instanceof CmdHeartbeat) {
            context.writeAndFlush(Codec.encode(context, cmd));
        } else if (cmd instanceof CmdClose) {
            shareGroup.closeClient((CmdClose) cmd);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;

            if (e.state() == IdleState.READER_IDLE) {
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage(), cause);
    }
}
