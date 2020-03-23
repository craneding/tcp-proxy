package com.dinghz.tcpproxy.nat.server.service;

import com.dinghz.tcpproxy.nat.core.cmd.AbstractCmd;
import com.dinghz.tcpproxy.nat.core.cmd.CmdClose;
import com.dinghz.tcpproxy.nat.core.cmd.CmdNetData;
import com.dinghz.tcpproxy.nat.core.codec.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShareGroup
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class ShareGroup {

    private volatile ChannelHandlerContext natProxyServerContext = null;
    private Map<String, ChannelHandlerContext> clientMap = new ConcurrentHashMap<>();

    public void closeClients() {
        for (ChannelHandlerContext context : clientMap.values()) {
            context.close();
        }

        clientMap.clear();
    }

    public void closeClient(CmdClose cmdClose) {
        ChannelHandlerContext context = clientMap.remove(cmdClose.getId());

        if (context != null) {
            context.close();
        }
    }

    public void sendToClient(CmdNetData netData) {
        ChannelHandlerContext context = clientMap.get(netData.getId());

        if (context != null) {
            ByteBuf buf = context.alloc().buffer(netData.getData().length);
            buf.writeBytes(netData.getData());
            context.writeAndFlush(buf);
        }
    }

    public <T extends AbstractCmd> void sendToInnerNat(T cmd) {
        if (natProxyServerContext != null) {
            natProxyServerContext.writeAndFlush(Codec.encode(natProxyServerContext, cmd));
        }
    }

    public ChannelHandlerContext getNatProxyServerContext() {
        return natProxyServerContext;
    }

    public void setNatProxyServerContext(ChannelHandlerContext natProxyServerContext) {
        this.natProxyServerContext = natProxyServerContext;
    }

    public void putClientContext(String id, ChannelHandlerContext clientContext) {
        clientMap.put(id, clientContext);
    }

    public void removeClientContext(String id) {
        clientMap.remove(id);
    }
}
