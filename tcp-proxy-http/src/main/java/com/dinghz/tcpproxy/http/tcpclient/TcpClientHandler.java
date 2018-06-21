package com.dinghz.tcpproxy.http.tcpclient;

import com.dinghz.tcpproxy.http.cache.TcpCache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * TcpClientHandler
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class TcpClientHandler extends ChannelInboundHandlerAdapter {
    static final Logger logger = LoggerFactory.getLogger(TcpClientHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.channel()
                .attr(AttributeKey.<List<ByteBuf>>valueOf("byteBufs"))
                .set(new ArrayList<>());

        logger.info("connect {} {}", ctx.channel().localAddress(), ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        List<ByteBuf> byteBufs = ctx.channel()
                .attr(AttributeKey.<List<ByteBuf>>valueOf("byteBufs"))
                .get();

        synchronized (byteBufs) {
            byteBufs.add(Unpooled.wrappedBuffer((ByteBuf) msg));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String tcpId = ctx.channel().attr(AttributeKey.<String>valueOf("tcpId")).get();

        TcpCache.removeTcpProxy(tcpId);

        logger.info("close {} {}", ctx.channel().localAddress(), ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);

        ctx.close();
    }
}
