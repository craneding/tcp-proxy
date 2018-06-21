/**
 *
 */
package com.dinghz.tcpproxy.http.servlet;

import com.dinghz.tcpproxy.http.cache.TcpCache;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TcpWrite extends HttpServlet {
    private static final long serialVersionUID = -7461565608023272179L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tcpId = req.getHeader("tcpid");

        ServletInputStream inputStream = req.getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        do {
            int read = inputStream.read();

            if (read == -1)
                break;

            os.write(read);
        } while (true);
        os.close();

        byte[] tcpData = os.toByteArray();

        ChannelFuture channelFuture = TcpCache.getTcpProxy(tcpId);

        if (channelFuture == null) {
            resp.getWriter().write("MyErr:socket is closed(code:01)");
            resp.setStatus(500);

            return;
        }

        Channel channel = channelFuture.channel();
        if (!channel.isOpen()) {
            resp.getWriter()
                    .write("MyErr:socket is closed(code:02:" + channel.localAddress() + " - " + channel.remoteAddress() + ")");
            resp.setStatus(500);

            return;
        }

        channelFuture.channel().writeAndFlush(Unpooled.wrappedBuffer(tcpData));
    }
}
