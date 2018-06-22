/**
 *
 */
package com.dinghz.tcpproxy;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;

public class Util {

    public static enum Parameters {
        tcpid, tcpdata, tcphost, tcpport, tcpip, tcpuser, tcppasswd
    }

    public static String getTcpId(HttpServletRequest req) throws IOException {
        return getParameter(req, Parameters.tcpid);
    }

    public static String getTcpHost(HttpServletRequest req) throws IOException {
        return getParameter(req, Parameters.tcphost);
    }

    public static int getTcpPort(HttpServletRequest req) throws NumberFormatException, IOException {
        return Integer.valueOf(getParameter(req, Parameters.tcpport));
    }

    public static String getParameter(HttpServletRequest req, Parameters parameters) throws IOException {
        String parameter = new String(req.getParameter(parameters.name()).getBytes("ISO8859_1"), "utf-8");

        if (parameter == null)
            throw new IOException(parameters.name() + " is not null.");

        return URLDecoder.decode(parameter, "utf-8");
    }

}
