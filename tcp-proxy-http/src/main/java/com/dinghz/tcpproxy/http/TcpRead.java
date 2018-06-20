/**
 *
 */
package com.dinghz.tcpproxy.http;

import com.dinghz.tcpproxy.Util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;

public class TcpRead extends HttpServlet {
    private static final long serialVersionUID = 3050911425512011438L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tcpId = Util.getTcpId(req);

        TcpProxy tcpProxy = TcpCache.getTcpProxy(tcpId);

        boolean pass = false;
        try {
            byte[] bs = tcpProxy.read();

            if (bs != null) {
                resp.getOutputStream().write(bs);
                resp.getOutputStream().flush();
            }

            resp.setStatus(HttpURLConnection.HTTP_OK);

            pass = true;
        } finally {
            if (!pass) {
                try {
                    tcpProxy.disconnect();
                } catch (Exception e1) {
                }
            }

        }
    }
}
