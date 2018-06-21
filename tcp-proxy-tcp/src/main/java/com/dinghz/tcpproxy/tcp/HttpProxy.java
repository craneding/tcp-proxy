package com.dinghz.tcpproxy.tcp;

import com.dinghz.tcpproxy.Util;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * TcpConfig
 *
 * @author dinghz
 * @date 2018/6/20
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class HttpProxy {
    private static final Logger logger = LoggerFactory.getLogger(HttpProxy.class);
    
    public static byte[] tcpRead(String jdbcid, String username) throws IOException {
        String spec = TcpConfig.BASE_URL + "/TcpRead";

        Map<String, String> parms = new LinkedHashMap<>();
        parms.put("tcpid", jdbcid);

        byte[] bs = postAndGetData(spec, parms);

        if (bs.length > 0) {
            logger.info("<<" + Hex.encodeHexString(bs));
            logger.info(username + " << " + TcpConfig.REMOTE_HOST + ":" + TcpConfig.REMOTE_PORT + " " + bs.length + "字节", true);
        }

        return bs;
    }

    public static boolean tcpWrite(final String tcpid, String username, byte[] data) throws IOException {
        String spec = TcpConfig.BASE_URL + "/TcpWrite";

        logger.info(username + " >> " + TcpConfig.REMOTE_HOST + ":" + TcpConfig.REMOTE_PORT + " " + data.length + "字节", true);

        return upload(spec, tcpid, data);
    }

    public static boolean sendRegister(String jdbcid, String username, String passwd) throws IOException {
        String spec = TcpConfig.BASE_URL + "/TcpRegister";

        Map<String, String> parms = new LinkedHashMap<>();
        parms.put("tcpid", jdbcid);
        parms.put("tcphost", TcpConfig.REMOTE_HOST);
        parms.put("tcpport", TcpConfig.REMOTE_PORT + "");
        parms.put(Util.Parameters.tcpuser.name(), username);
        parms.put(Util.Parameters.tcppasswd.name(), passwd);

        try {
            return post(spec, parms);
        } catch (IOException e1) {
            logger.info("url:" + spec + " error:" + e1.getMessage());

            e1.printStackTrace();

            return false;
        }
    }

    public static boolean sendUnRegister(String jdbcid) {
        String spec = TcpConfig.BASE_URL + "/TcpUnRegister";
        Map<String, String> parms = new LinkedHashMap<>();
        parms.put("tcpid", jdbcid);

        try {
            return post(spec, parms);
        } catch (IOException e1) {
            logger.info("url:" + spec + " error:" + e1.getMessage());

            e1.printStackTrace();
        }

        return false;
    }

    static boolean post(String spec, Map<String, String> parms) throws IOException {
        StringBuilder builder = toPostDataBuilder(parms);

        HttpURLConnection conn = null;
        OutputStream out = null;

        try {
            URL url = new URL(spec);

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");
//            conn.setRequestProperty("Content-type", "text/html;charset=utf-8");
            conn.connect();

            if (builder.length() > 0) {
                byte[] bs = builder.toString().getBytes("utf-8");

                out = new BufferedOutputStream(conn.getOutputStream());
                out.write(bs);
                out.flush();
            }

            int responseCode = conn.getResponseCode();

            boolean b = responseCode == HttpURLConnection.HTTP_OK;

            if (!b) {
                logger.info(responseCode + " " + spec + " " + new String(readBytes(conn.getErrorStream())));
            }

            return b;
        } finally {
            if (out != null)
                out.close();

            if (conn != null)
                conn.disconnect();
        }
    }

    static byte[] postAndGetData(String spec, Map<String, String> parms) throws IOException {
        StringBuilder builder = toPostDataBuilder(parms);

        HttpURLConnection conn = null;
        OutputStream out = null;
        InputStream input = null;

        try {
            URL url = new URL(spec);

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");
            conn.connect();

            if (builder.length() > 0) {
                out = new BufferedOutputStream(conn.getOutputStream());
                out.write(builder.toString().getBytes("utf-8"));
                out.flush();
            }

            int responseCode = conn.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                input = new BufferedInputStream(conn.getErrorStream());

                throw new IOException(new String(readBytes(input)));
            }

            input = new BufferedInputStream(conn.getInputStream());

            return readBytes(input);
        } finally {
            if (out != null)
                out.close();

            if (input != null)
                input.close();

            if (conn != null)
                conn.disconnect();
        }
    }

    private static byte[] readBytes(InputStream input) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        do {
            byte[] bs = new byte[1024];

            int len = input.read(bs);

            if (len == -1)
                break;

            os.write(Arrays.copyOf(bs, len));
        } while (true);

        os.close();

        return os.toByteArray();
    }

    static boolean upload(String spec, String tcpid, byte[] data) throws IOException {
        HttpURLConnection conn = null;
        OutputStream out = null;

        try {
            URL url = new URL(spec);

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(1024 * 1024);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data;file=" + tcpid);
            conn.setRequestProperty("tcpid", tcpid);
            conn.connect();

            out = new BufferedOutputStream(conn.getOutputStream());
            out.write(data);
            out.flush();

            int responseCode = conn.getResponseCode();

            boolean b = responseCode == HttpURLConnection.HTTP_OK;

            if (!b) {
                logger.info(responseCode + " " + spec + " ", true);
            }

            return b;
        } finally {
            if (out != null)
                out.close();

            if (conn != null)
                conn.disconnect();
        }
    }

    protected static StringBuilder toPostDataBuilder(Map<String, String> parms)
            throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();

        Set<String> keySet = parms.keySet();
        for (String key : keySet) {
            if (builder.length() > 0)
                builder.append("&");

            builder.append(key).append("=").append(URLEncoder.encode(parms.get(key), "utf-8"));
        }

        if (builder.length() > 0)
            builder.append("&");
        builder.append("t=").append(System.currentTimeMillis());
        return builder;
    }

}
