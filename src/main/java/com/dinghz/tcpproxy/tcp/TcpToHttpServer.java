/**
 *
 */
package com.dinghz.tcpproxy.tcp;

import com.dinghz.tcpproxy.Config;
import com.dinghz.tcpproxy.Util;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.Md5Crypt;

import java.io.*;
import java.net.*;
import java.util.*;

public class TcpToHttpServer {

    static String baseUrl;
    static String remoteHost;
    static int remotePort;

    public static void main(String[] args) throws IOException {
        baseUrl = args[0];
        remoteHost = args[1];
        remotePort = Integer.valueOf(args[2]);
        Integer localport = Integer.valueOf(args[3]);

        String passwd = System.getenv("tcp_proxy_pwd");
        if (passwd == null || passwd.trim().isEmpty()) {
            passwd = System.getProperty("tcp_proxy_pwd");

            if (passwd == null || passwd.trim().isEmpty()) {
                Scanner scanner = new Scanner(System.in);
                PrintStream printStream = new PrintStream(System.out);

                printStream.print("passwd:");
                passwd = scanner.nextLine();
            }
        }

        try (ServerSocket server = new ServerSocket(localport)) {
            Util.log("启动成功:" + localport, true);

            do {
                final Socket client = server.accept();

                final String jdbcid = UUID.randomUUID().toString();

                String username = Config.ips.get(client.getInetAddress().getHostAddress());

                if (username == null) {
                    Util.log("新连接" + client + " " + jdbcid + " " + (username == null ? "unknown" : username) + " Wrong.", true);

                    try {
                        client.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    continue;
                } else
                    Util.log("新连接" + client + " " + jdbcid + " " + (username == null ? "unknown" : username) + " Bingo.", true);

                newTask(client, jdbcid, username, Md5Crypt.md5Crypt(passwd.trim().getBytes(), Config.salt));
            } while (true);
        }

    }

    protected static void newTask(final Socket client, final String jdbcid, final String username, final String passwd) {
        final Runnable readRunnable = new Runnable() {
            @Override
            public void run() {
                startRead(client, jdbcid);
            }
        };

        final Runnable writeRunnable = new Runnable() {
            @Override
            public void run() {
                startWrite(client, jdbcid);
            }
        };

        Runnable registerRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    client.setKeepAlive(true);

                    if (sendRegister(jdbcid, username, passwd)) {
                        runInThread(readRunnable);

                        runInThread(writeRunnable);
                    } else {
                        try {
                            Util.log("关闭连接" + client, true);

                            client.close();
                        } catch (IOException e1) {
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    try {
                        Util.log("关闭连接" + client, true);

                        client.close();
                    } catch (IOException e1) {
                    }
                }
            }
        };

        runInThread(registerRunnable);
    }

    private static void startRead(Socket client, String jdbcid) {
        String spec = baseUrl + "/TcpRead";

        Map<String, String> parms = new LinkedHashMap<>();
        parms.put("tcpid", jdbcid);

        try (OutputStream out = new BufferedOutputStream(client.getOutputStream())) {
            do {
                byte[] bs = postAndGetData(spec, parms);

                Util.log("<<" + Hex.encodeHexString(bs));
                Util.log(jdbcid + "收到" + bs.length + "字节", true);

                if (bs.length > 0) {
                    out.write(bs);
                    out.flush();
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            } while (true);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().indexOf("java.net.SocketException: Socket closed") != -1) {
            } else {
                e.printStackTrace();
            }
        } finally {
            try {
                Util.log("关闭连接" + client, true);
                client.close();
            } catch (IOException e) {
            }

            sendUnRegister(jdbcid);
        }
    }

    private static void startWrite(final Socket client, final String tcpid) {
        try (InputStream in = new BufferedInputStream(client.getInputStream());) {
            boolean status;
            do {
                byte[] bs = new byte[1024 * 3];
                int len = in.read(bs);

                if (len == -1)
                    break;

                bs = Arrays.copyOf(bs, len);

                //String encodeHexString = Hex.encodeHexString(bs);
                //String encodeHexString = Base64.encodeBase64String(bs);
                /*
                Util.log(">>" + encodeHexString);
				
				String spec = baseUrl + "/TcpWrite";
				
				Map<String, String> parms = new LinkedHashMap<String, String>();
				parms.put("tcpid", tcpid);
				parms.put("tcpdata", encodeHexString);
				
				status = post(spec, parms);
				*/

                String spec = baseUrl + "/TcpWrite";

                Util.log(tcpid + "发送" + bs.length + "字节", true);

                status = upload(spec, tcpid, bs);
            } while (status);
        } catch (IOException e) {
        } finally {
            try {
                Util.log("关闭连接" + client, true);

                client.close();
            } catch (IOException e) {
            }

            sendUnRegister(tcpid);
        }
    }

    public static boolean sendRegister(String jdbcid, String username, String passwd) throws IOException {
        String spec = baseUrl + "/TcpRegister";

        Map<String, String> parms = new LinkedHashMap<>();
        parms.put("tcpid", jdbcid);
        parms.put("tcphost", remoteHost);
        parms.put("tcpport", remotePort + "");
        parms.put(Util.Parameters.tcpuser.name(), username);
        parms.put(Util.Parameters.tcppasswd.name(), passwd);

        return post(spec, parms);
    }

    public static boolean sendUnRegister(String jdbcid) {
        String spec = baseUrl + "/TcpUnRegister";
        Map<String, String> parms = new LinkedHashMap<>();
        parms.put("tcpid", jdbcid);

        try {
            return post(spec, parms);
        } catch (IOException e1) {
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
                Util.log(responseCode + " " + spec + " " + new String(readBytes(conn.getErrorStream())));
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
                Util.log(responseCode + " " + spec + " ", true);
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

    public static Thread runInThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();

        return thread;
    }
}
