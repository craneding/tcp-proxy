package com.dinghz.tcpproxy.tcp.core;

import com.dinghz.tcpproxy.domain.*;
import com.dinghz.tcpproxy.tcp.domain.TcpConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
@Slf4j
public class HttpProxy {

    public static byte[] tcpRead(RestTemplate restTemplate, TcpConfig tcpConfig, String jdbcid, String username) throws IOException {
        String spec = tcpConfig.getBaseUrl() + "/TcpRead";

        ReadRequest readRequest = new ReadRequest();
        readRequest.setTcpId(jdbcid);

        try {
            ReadResponse response = restTemplate.postForObject(spec, readRequest, ReadResponse.class);

            if (response != null) {
                if (response.getBody() != null && response.getBody().length > 0) {
                    //logger.info("<<" + Hex.encodeHexString(bs));
                    log.info(username + " << " + tcpConfig.getRemoteHost() + ":" + tcpConfig.getRemotePort() + " " + response.getBody().length + "字节", true);
                } else if (!"0".equals(response.getCode())) {
                    return null;
                }

                return response.getBody();
            }
        } catch (Exception e1) {
            log.info("url:" + spec + " error:" + e1.getMessage());

            e1.printStackTrace();

            return new byte[]{};
        }

        return new byte[]{};
    }

    public static boolean tcpWrite(RestTemplate restTemplate, TcpConfig tcpConfig, String tcpid, String username, byte[] data) throws IOException {
        String spec = tcpConfig.getBaseUrl() + "/TcpWrite";

        WriteRequest writeRequest = new WriteRequest();
        writeRequest.setTcpId(tcpid);
        writeRequest.setBody(data);

        //logger.info(">>" + Hex.encodeHexString(data));
        log.info(username + " >> " + tcpConfig.getRemoteHost() + ":" + tcpConfig.getRemotePort() + " " + data.length + "字节", true);

        try {
            WriteResponse response = restTemplate.postForObject(spec, writeRequest, WriteResponse.class);

            return response != null && "0".equals(response.getCode());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean sendRegister(RestTemplate restTemplate, TcpConfig tcpConfig, String jdbcid, String ip, String username, String passwd) throws IOException {
        String spec = tcpConfig.getBaseUrl() + "/TcpRegister";

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setTcpId(jdbcid);
        registerRequest.setTcpHost(tcpConfig.getRemoteHost());
        registerRequest.setTcpPort(tcpConfig.getRemotePort());
        registerRequest.setTcpIp(ip);
        registerRequest.setTcpUser(username);
        registerRequest.setTcpPasswd(passwd);

        try {
            RegisterResponse response = restTemplate.postForObject(spec, registerRequest, RegisterResponse.class);

            return response != null && "0".equals(response.getCode());
        } catch (Exception e1) {
            log.info("url:" + spec + " error:" + e1.getMessage());

            e1.printStackTrace();

            return false;
        }
    }

    public static boolean sendUnRegister(RestTemplate restTemplate, TcpConfig tcpConfig, String jdbcid) {
        String spec = tcpConfig.getBaseUrl() + "/TcpUnRegister";

        UnRegisterRequest unRegisterRequest = new UnRegisterRequest();
        unRegisterRequest.setTcpId(jdbcid);

        try {
            UnRegisterResponse response = restTemplate.postForObject(spec, unRegisterRequest, UnRegisterResponse.class);
            return response != null && "0".equals(response.getCode());
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
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
                log.info(responseCode + " " + spec + " " + new String(readBytes(conn.getErrorStream())));
            }

            return b;
        } finally {
            if (out != null) {
                out.close();
            }

            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    static byte[] postAndGetData(String spec, Map<String, String> parms) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(parms);

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
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.connect();

            /*
            if (builder.length() > 0) {
                out = new BufferedOutputStream(conn.getOutputStream());
                out.write(builder.toString().getBytes("utf-8"));
                out.flush();
            }
             */

            out.write(json.getBytes(StandardCharsets.UTF_8));
            out.flush();

            int responseCode = conn.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                input = new BufferedInputStream(conn.getErrorStream());

                throw new IOException(new String(readBytes(input)));
            }

            input = new BufferedInputStream(conn.getInputStream());

            return readBytes(input);
        } finally {
            if (out != null) {
                out.close();
            }

            if (input != null) {
                input.close();
            }

            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static byte[] readBytes(InputStream input) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        do {
            byte[] bs = new byte[1024];

            int len = input.read(bs);

            if (len == -1) {
                break;
            }

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
                log.info(responseCode + " " + spec + " ", true);
            }

            return b;
        } finally {
            if (out != null) {
                out.close();
            }

            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    protected static StringBuilder toPostDataBuilder(Map<String, String> parms)
            throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();

        Set<String> keySet = parms.keySet();
        for (String key : keySet) {
            if (builder.length() > 0) {
                builder.append("&");
            }

            builder.append(key).append("=").append(URLEncoder.encode(parms.get(key), "utf-8"));
        }

        if (builder.length() > 0) {
            builder.append("&");
        }
        builder.append("t=").append(System.currentTimeMillis());
        return builder;
    }

}
