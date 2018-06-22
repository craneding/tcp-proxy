package com.dinghz.tcpproxy.tcp;

import com.dinghz.tcpproxy.Config;
import com.dinghz.tcpproxy.cert.Cert;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TcpServerHandler
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class TcpServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TcpServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        final String jdbcid = UUID.randomUUID().toString();
        ctx.channel().attr(AttributeKey.valueOf("jdbcid")).set(jdbcid);

        String passwd, username;
        final InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        final String ip = remoteAddress.getAddress().getHostAddress();

        if (Config.TCP_PROXY_AUTH) {
            passwd = Cert.getNewInstance().passwd(ip, Config.TCP_PROXY_AUTHCODE);
            username = Cert.getNewInstance().hasCert(ip, passwd);
        } else {
            passwd = "NOPASSWD";
            username = "NOUSERNAME";
        }

        ctx.channel().attr(AttributeKey.valueOf("username")).set(username);

        if (username == null || username.trim().isEmpty()) {
            logger.info("新连接" + remoteAddress + " " + jdbcid + " " + (username == null ? "unknown" : username) + " Wrong.");

            ctx.close();

            return;
        } else {
            logger.info("新连接" + remoteAddress + " " + jdbcid + " " + (username == null ? "unknown" : username) + " Bingo.(remoteIP:" + TcpConfig.REMOTE_HOST + ", remotePort:" + TcpConfig.REMOTE_PORT + ")");
        }

        if (HttpProxy.sendRegister(jdbcid, ip, username, passwd)) {
            ctx.channel().attr(AttributeKey.valueOf("register")).set(true);

            logger.info("认证成功 {} {}", jdbcid, username);
        } else {
            logger.info("认证失败 {} {}", jdbcid, username);

            ctx.close();

            return;
        }

        AtomicBoolean live = new AtomicBoolean(true);
        ctx.channel().attr(AttributeKey.valueOf("live")).set(live);

        new Thread(() -> {
            try {
                do {
                    byte[] data = HttpProxy.tcpRead(jdbcid, username);

                    if (data.length > 0) {
                        ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(data));
                    }

                    Thread.sleep(50);
                } while (ctx.channel().isOpen() && live.get());
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().startsWith("MyErr:")) {
                    logger.error(e.getMessage());
                } else {
                    logger.error(e.getMessage(), e);
                }

                ctx.close();
            }
        }).start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        String jdbcid = ctx.channel().attr(AttributeKey.<String>valueOf("jdbcid")).get();
        String username = ctx.channel().attr(AttributeKey.<String>valueOf("username")).get();

        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] value = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(value);

        HttpProxy.tcpWrite(jdbcid, username, value);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);

        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String jdbcid = ctx.channel().attr(AttributeKey.<String>valueOf("jdbcid")).get();
        String username = ctx.channel().attr(AttributeKey.<String>valueOf("username")).get();
        Boolean register = ctx.channel().attr(AttributeKey.<Boolean>valueOf("register")).get();
        AtomicBoolean live = ctx.channel().attr(AttributeKey.<AtomicBoolean>valueOf("live")).get();

        if (live != null) {
            live.set(false);
        }

        logger.info("关闭连接 {} {}", jdbcid, username);

        if (register != null && register) {
            ctx.channel().attr(AttributeKey.<Boolean>valueOf("register")).set(null);

            HttpProxy.sendUnRegister(jdbcid);
        }
    }

}
