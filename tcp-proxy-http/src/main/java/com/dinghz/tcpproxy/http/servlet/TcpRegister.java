/**
 *
 */
package com.dinghz.tcpproxy.http.servlet;

import com.dinghz.tcpproxy.Config;
import com.dinghz.tcpproxy.Util;
import com.dinghz.tcpproxy.cert.Cert;
import com.dinghz.tcpproxy.http.cache.TcpCache;
import com.dinghz.tcpproxy.http.tcpclient.TcpClient;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TcpRegister extends HttpServlet {
    private static final long serialVersionUID = -8421614927735092760L;

    private static final Logger logger = LoggerFactory.getLogger(TcpRegister.class);

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tcpId = Util.getTcpId(req);
        String tcpHost = Util.getTcpHost(req);
        int tcpPort = Util.getTcpPort(req);
        String ip = Util.getParameter(req, Util.Parameters.tcpip);
        String username = Util.getParameter(req, Util.Parameters.tcpuser);
        String passwd = Util.getParameter(req, Util.Parameters.tcppasswd);

        String readUserName;

        if (Config.TCP_PROXY_AUTH) {
            readUserName = Cert.getNewInstance().hasCert(ip, passwd);
        } else {
            readUserName = username;
        }

        if (readUserName == null || readUserName.trim().isEmpty() || !readUserName.equals(username)) {
            logger.info("check:" + username + " " + passwd + " so bad", true);

            resp.getWriter().write("MyErr:username or passwd is wrong(code:03)");
            resp.setStatus(500);

            return;
        } else {
            logger.info("check:" + ip + " " + username + " good", true);
        }

        try {
            ChannelFuture channelFuture = TcpClient.instance().connect(tcpHost, tcpPort);
            channelFuture.channel().attr(AttributeKey.valueOf("tcpId")).set(tcpId);
            TcpCache.newTcpProxy(tcpId, channelFuture);
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }

        logger.info("register {}", tcpId);
    }

}
