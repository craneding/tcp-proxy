package com.dinghz.tcpproxy.tcp.core;

import com.dinghz.tcpproxy.Config;
import com.dinghz.tcpproxy.cert.Cert;
import com.dinghz.tcpproxy.tcp.domain.TcpConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@Slf4j
public class TcpServerHandler extends SimpleChannelInboundHandler<Object> {
    private final TcpConfig tcpConfig;
    private final RestTemplate restTemplate;

    public TcpServerHandler(TcpConfig tcpConfig, RestTemplate restTemplate) {
        this.tcpConfig = tcpConfig;
        this.restTemplate = restTemplate;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        final String jdbcid = UUID.randomUUID().toString();
        ctx.channel().attr(AttributeKey.valueOf("jdbcid")).set(jdbcid);

        String passwd, username;
        final InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        final String ip = remoteAddress.getAddress().getHostAddress();

        if (Config.TCP_PROXY_AUTH) {
            passwd = Cert.getNewInstance().passwd(ip, Config.TCP_PROXY_AUTHCODE);
            if (passwd != null && !passwd.trim().isEmpty()) {
                username = Cert.getNewInstance().hasCert(ip, passwd);
            } else {
                username = null;
            }
        } else {
            passwd = "NOPASSWD";
            username = "NOUSERNAME";
        }

        ctx.channel().attr(AttributeKey.valueOf("username")).set(username);

        if (username == null || username.trim().isEmpty()) {
            log.info("新连接" + remoteAddress + " " + jdbcid + " " + (username == null ? "unknown" : username) + " Wrong.");

            ctx.close();

            return;
        } else {
            log.info("新连接" + remoteAddress + " " + jdbcid + " " + (username == null ? "unknown" : username) + " Bingo.(remoteIP:" + tcpConfig
                    .getRemoteHost() + ", remotePort:" + tcpConfig.getRemotePort() + ")");
        }

        if (HttpProxy.sendRegister(restTemplate, tcpConfig, jdbcid, ip, username, passwd)) {
            ctx.channel().attr(AttributeKey.valueOf("register")).set(true);

            log.info("认证成功 {} {}", jdbcid, username);
        } else {
            log.info("认证失败 {} {}", jdbcid, username);

            ctx.close();

            return;
        }

        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.setId(jdbcid);
        sessionInfo.setUserName(username);
        sessionInfo.setUserIp(ip);
        sessionInfo.setRemoteIp(tcpConfig.getRemoteHost());
        sessionInfo.setRemotePort(tcpConfig.getRemotePort());
        sessionInfo.setActiveTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss S")));
        sessionInfo.setRemark(tcpConfig.getName());
        Cache.SESSION_MAP.put(jdbcid, sessionInfo);

        AtomicBoolean live = new AtomicBoolean(true);
        ctx.channel().attr(AttributeKey.valueOf("live")).set(live);

        new Thread(() -> {
            try {
                do {
                    byte[] data = HttpProxy.tcpRead(restTemplate, tcpConfig, jdbcid, username);

                    if(data == null) {
                        ctx.close();

                        break;
                    }

                    if (data.length > 0) {
                        ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(data));
                    }

                    Thread.sleep(50);
                } while (ctx.channel().isOpen() && live.get());
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().startsWith("MyErr:")) {
                    log.error(e.getMessage());
                } else {
                    log.error(e.getMessage(), e);
                }

                ctx.close();
            }
        }).start();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws IOException {
        String jdbcid = ctx.channel().attr(AttributeKey.<String>valueOf("jdbcid")).get();
        String username = ctx.channel().attr(AttributeKey.<String>valueOf("username")).get();

        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] value = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(value);

        HttpProxy.tcpWrite(restTemplate, tcpConfig, jdbcid, username, value);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);

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

        log.info("关闭连接 {} {}", jdbcid, username);

        Cache.SESSION_MAP.remove(jdbcid);

        if (register != null && register) {
            ctx.channel().attr(AttributeKey.<Boolean>valueOf("register")).set(null);

            HttpProxy.sendUnRegister(restTemplate, tcpConfig, jdbcid);
        }
    }

}
