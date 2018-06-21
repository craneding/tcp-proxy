/**
 *
 */
package com.dinghz.tcpproxy.http.servlet;

import com.dinghz.tcpproxy.Util;
import com.dinghz.tcpproxy.http.cache.TcpCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TcpUnRegister extends HttpServlet {
    private static final long serialVersionUID = 102635370400698962L;

    private static final Logger logger = LoggerFactory.getLogger(TcpRegister.class);

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tcpId = Util.getTcpId(req);

        TcpCache.removeTcpProxy(tcpId);

        logger.info("unregister {}", tcpId);
    }

}
