/**
 *
 */
package com.dinghz.tcpproxy;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;

public class Util {

    private static final boolean logEnable = Boolean.valueOf(System.getProperty("log.enable", "true"));

    public static enum Parameters {
        tcpid, tcpdata, tcphost, tcpport
    }

    public static String getTcpId(HttpServletRequest req) throws IOException {
        return getParameter(req, Parameters.tcpid);
    }

    public static byte[] getTcpDataByHex(HttpServletRequest req) throws IOException, DecoderException {
        String parameter = getParameter(req, Parameters.tcpdata);

        return Hex.decodeHex(parameter.toCharArray());
    }

    public static byte[] getTcpDataByBase64(HttpServletRequest req) throws IOException {
        String parameter = getParameter(req, Parameters.tcpdata);

        return Base64.decodeBase64(parameter);
    }

    public static String getTcpHost(HttpServletRequest req) throws IOException {
        return getParameter(req, Parameters.tcphost);
    }

    public static int getTcpPort(HttpServletRequest req) throws NumberFormatException, IOException {
        return Integer.valueOf(getParameter(req, Parameters.tcpport));
    }

    static String getParameter(HttpServletRequest req, Parameters parameters) throws IOException {
        String parameter = req.getParameter(parameters.name());

        if (parameter == null)
            throw new IOException(parameters.name() + " is not null.");

        return URLDecoder.decode(parameter, "utf-8");
    }

    public static void log(String txt) {
        if (logEnable)
            System.out.println(txt);
    }
}
