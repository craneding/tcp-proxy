/**
 *
 */
package com.dinghz.tcpproxy.http.servlet;

import com.dinghz.tcpproxy.Util;
import com.dinghz.tcpproxy.http.cache.TcpCache;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class TcpRead extends HttpServlet {
    private static final long serialVersionUID = 3050911425512011438L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean pass = false;

        ChannelFuture channelFuture = TcpCache.getTcpProxy(Util.getTcpId(req));

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

        try {
            channel.attr(AttributeKey.valueOf("bu"));

            List<ByteBuf> byteBufs = channel
                    .attr(AttributeKey.<List<ByteBuf>>valueOf("byteBufs"))
                    .get();

            synchronized (byteBufs) {
                for (ByteBuf byteBuf : byteBufs) {
                    int len = byteBuf.readableBytes();

                    if (len <= 0) {
                        continue;
                    }

                    byte[] bs = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(bs);

                    resp.getOutputStream().write(bs);
                    resp.getOutputStream().flush();
                }

                byteBufs.clear();
            }

            pass = true;
        } finally {
            if (!pass) {
                try {
                    channel.close();
                } catch (Exception e1) {
                }
            }
        }
    }
}
