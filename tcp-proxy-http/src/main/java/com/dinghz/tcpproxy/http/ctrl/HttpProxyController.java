package com.dinghz.tcpproxy.http.ctrl;

import com.dinghz.tcpproxy.Config;
import com.dinghz.tcpproxy.cert.Cert;
import com.dinghz.tcpproxy.domain.*;
import com.dinghz.tcpproxy.http.cache.TcpCache;
import com.dinghz.tcpproxy.http.tcpclient.TcpClient;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author dinghz
 * @date 2021/11/6
 * @company 广州车小Y科技有限公司
 * @email dinghz@gzyztd.com
 */
@RestController
public class HttpProxyController {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyController.class);

    @PostConstruct
    public void init() {
        TcpClient.instance().start();
    }

    @PreDestroy
    public void destory() {
        TcpClient.instance().stop();
    }

    @PostMapping("/tcp/TcpRegister")
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        final RegisterResponse response = new RegisterResponse();

        String tcpId = request.getTcpId();
        String tcpHost = request.getTcpHost();
        int tcpPort = request.getTcpPort();
        String ip = request.getTcpIp();
        String username = request.getTcpUser();
        String passwd = request.getTcpPasswd();

        String readUserName;

        if (Config.TCP_PROXY_AUTH) {
            readUserName = Cert.getNewInstance().hasCert(ip, passwd);
        } else {
            readUserName = username;
        }

        if (readUserName == null || readUserName.trim().isEmpty() || !readUserName.equals(username)) {
            logger.info("check:" + username + " " + passwd + " so bad", true);

            response.setCode("03");
            response.setMsg("MyErr:username or passwd is wrong(code:03)");
            return response;
        } else {
            logger.info("check:" + ip + " " + username + " good", true);
        }

        try {
            ChannelFuture channelFuture = TcpClient.instance().connect(tcpHost, tcpPort);
            channelFuture.channel().attr(AttributeKey.valueOf("tcpId")).set(tcpId);

            channelFuture.awaitUninterruptibly(5, TimeUnit.SECONDS);
            if (channelFuture.isCancelled()) {
                // Connection attempt cancelled by user
                throw new RuntimeException("Connection attempt cancelled by user");
            } else if (!channelFuture.isSuccess()) {
                // You might get a NullPointerException here because the future
                // might not be completed yet.
                throw channelFuture.cause();
            } else {
                // Connection established successfully
                TcpCache.newTcpProxy(tcpId, channelFuture);
            }
        } catch (Throwable e) {
            response.setCode("9999");
            response.setMsg(e.getMessage());
            return response;
        }

        logger.info("register {}", tcpId);

        response.setCode("0");
        return response;
    }

    @PostMapping("/tcp/TcpRead")
    public ReadResponse read(@RequestBody ReadRequest request) throws IOException {
        final ReadResponse response = new ReadResponse();

        boolean pass = false;

        ChannelFuture channelFuture = TcpCache.getTcpProxy(request.getTcpId());

        if (channelFuture == null) {
            response.setCode("01");
            response.setMsg("MyErr:socket is closed(code:01)");
            return response;
        }

        Channel channel = channelFuture.channel();
        if (!channel.isOpen()) {
            response.setCode("02");
            response.setMsg("MyErr:socket is closed(code:02:" + channel.localAddress() + " - " + channel.remoteAddress() + ")");
            return response;
        }

        try {
            List<byte[]> byteBufs = channel
                    .attr(AttributeKey.<List<byte[]>>valueOf("byteBufs"))
                    .get();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            synchronized (byteBufs) {
                for (byte[] bs : byteBufs) {
                    os.write(bs);
                }

                byteBufs.clear();
            }

            pass = true;

            response.setCode("0");
            response.setBody(os.toByteArray());
            return response;
        } finally {
            if (!pass) {
                try {
                    channel.close();
                } catch (Exception e1) {
                }
            }
        }
    }

    @PostMapping("/tcp/TcpWrite")
    public WriteResponse write(@RequestBody WriteRequest request) {
        final WriteResponse response = new WriteResponse();

        String tcpId = request.getTcpId();
        byte[] tcpData = request.getBody();

        ChannelFuture channelFuture = TcpCache.getTcpProxy(tcpId);

        if (channelFuture == null) {
            response.setCode("01");
            response.setMsg("MyErr:socket is closed(code:01)");
            return response;
        }

        Channel channel = channelFuture.channel();
        if (!channel.isOpen()) {
            response.setCode("02");
            response.setMsg("MyErr:socket is closed(code:02:" + channel.localAddress() + " - " + channel.remoteAddress() + ")");
            return response;
        }

        channelFuture.channel().writeAndFlush(Unpooled.wrappedBuffer(tcpData));

        response.setCode("0");
        response.setMsg("");
        return response;
    }

    @PostMapping("/tcp/TcpUnRegister")
    public UnRegisterResponse unRegister(@RequestBody UnRegisterRequest req) {
        String tcpId = req.getTcpId();

        TcpCache.removeTcpProxy(tcpId);

        logger.info("unregister {}", tcpId);

        final UnRegisterResponse response = new UnRegisterResponse();
        response.setCode("0000");
        return response;
    }

}
