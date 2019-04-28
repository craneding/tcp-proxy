package com.dinghz.tcpproxy.http.tcpclient;

import com.dinghz.tcpproxy.http.cache.TcpCache;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
                .attr(AttributeKey.<List<byte[]>>valueOf("byteBufs"))
                .set(new ArrayList<>());

        logger.info("connect {} {}", ctx.channel().localAddress(), ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buffer = (ByteBuf) msg;

        final ByteArrayOutputStream os = new ByteArrayOutputStream(buffer.readableBytes());
        try {
            while (buffer.isReadable()) {
                os.write(buffer.readByte());
            }
        } finally {
            buffer.release();
        }

        if (os.size() > 0) {
            List<byte[]> byteBufs = ctx.channel()
                    .attr(AttributeKey.<List<byte[]>>valueOf("byteBufs"))
                    .get();

            synchronized (byteBufs) {
                byteBufs.add(os.toByteArray());
            }
        }

        try {
            os.close();
        } catch (IOException e) {
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

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;

            switch (event.state()) {
                case ALL_IDLE:
                    logger.warn(event.state().toString());

                    ctx.close();

                    break;
                default:
                    break;
            }
        }
    }
}
