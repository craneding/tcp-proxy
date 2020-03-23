package com.dinghz.tcpproxy.nat.client.service;

import com.dinghz.tcpproxy.nat.core.cmd.*;
import com.dinghz.tcpproxy.nat.core.codec.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NatProxyClientHandler
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@Slf4j
@ChannelHandler.Sharable
public class NatProxyClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final Map<String, NatTcpClient> clientMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        AbstractCmd cmd = Codec.decode(buf);

        if (cmd instanceof CmdConnect) {
            CmdConnect c = (CmdConnect) cmd;

            // 创建真实的连接
            NatTcpClient tcpClient = new NatTcpClient(c.getRealHost(), c.getRealPort(), new NatRealClientInitializer(c.getId(), ctx));
            tcpClient.start();
            tcpClient.connect();

            // 连接缓存到内存
            clientMap.put(cmd.getId(), tcpClient);
        } else if (cmd instanceof CmdNetData) {
            CmdNetData c = (CmdNetData) cmd;

            NatTcpClient tcpClient = clientMap.get(c.getId());
            if (tcpClient != null) {
                // 转发数据到真实的连接
                try {
                    tcpClient.send(c.getData());
                } catch (Exception e) {
                    // 异常,关闭连接,返回关闭信号
                    tcpClient.stop();
                    clientMap.remove(c.getId());
                }
            } else {
                // 真实的连接不存在,返回关闭信号
                CmdClose cmdClose = new CmdClose();
                cmdClose.setId(c.getId());
                ctx.writeAndFlush(Codec.encode(ctx, cmdClose));
            }
        } else if (cmd instanceof CmdClose) {
            // 关闭信号,关闭真实连接
            NatTcpClient tcpClient = clientMap.get(cmd.getId());
            if (tcpClient != null) {
                tcpClient.stop();

                clientMap.remove(cmd.getId());
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        for (NatTcpClient tcpClient : clientMap.values()) {
            try {
                tcpClient.stop();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        clientMap.clear();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;

            if (e.state() == IdleState.READER_IDLE) {
                ctx.close();
            } else if (e.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(Codec.encode(ctx, new CmdHeartbeat()));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage(), cause);

        ctx.close();
    }

}
