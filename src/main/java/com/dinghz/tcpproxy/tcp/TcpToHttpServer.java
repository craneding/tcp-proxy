/**
 *
 */
package com.dinghz.tcpproxy.tcp;

import com.dinghz.tcpproxy.Util;
import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TcpToHttpServer {

    static String baseUrl;
    static String remoteHost;
    static int remotePort;

    public static void main(String[] args) throws IOException {
        baseUrl = args[0];
        remoteHost = args[1];
        remotePort = Integer.valueOf(args[2]);
        Integer localport = Integer.valueOf(args[3]);

        try (ServerSocket server = new ServerSocket(localport)) {
            do {
                final Socket client = server.accept();

                final String jdbcid = UUID.randomUUID().toString();

                Util.log("新连接" + client + " " + jdbcid);

                newTask(client, jdbcid);
            } while (true);
        }

    }

    protected static void newTask(final Socket client, final String jdbcid) {
        final Runnable readRunnable = () -> startRead(client, jdbcid);

        final Runnable writeRunnable = () -> startWrite(client, jdbcid);

        Runnable registerRunnable = () -> {
            try {
                client.setKeepAlive(true);

                if (sendRegister(jdbcid)) {
                    runInThread(readRunnable);

                    runInThread(writeRunnable);
                } else {
                    try {
                        client.close();
                    } catch (IOException e1) {
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

                try {
                    client.close();
                } catch (IOException e1) {
                }
            }
        };

        runInThread(registerRunnable);
    }

    private static void startRead(Socket client, String jdbcid) {
        String spec = baseUrl + "/TcpRead";

        Map<String, String> parms = new LinkedHashMap<>();
        parms.put("tcpid", jdbcid);

        try (OutputStream out = new BufferedOutputStream(client.getOutputStream());) {
            do {
                byte[] bs = postAndGetData(spec, parms);

                Util.log("<<" + Hex.encodeHexString(bs));

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
            e.printStackTrace();
        } finally {
            try {
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

                status = upload(spec, tcpid, bs);
            } while (status);
        } catch (IOException e) {
        } finally {
            try {
                client.close();
            } catch (IOException e) {
            }

            sendUnRegister(tcpid);
        }
    }

    public static boolean sendRegister(String jdbcid) throws IOException {
        String spec = baseUrl + "/TcpRegister";

        Map<String, String> parms = new LinkedHashMap<>();
        parms.put("tcpid", jdbcid);
        parms.put("tcphost", remoteHost);
        parms.put("tcpport", remotePort + "");

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
                Util.log(responseCode + " " + spec + " ");
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

            if (responseCode != HttpURLConnection.HTTP_OK)
                throw new IOException(conn.getResponseMessage());

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            input = new BufferedInputStream(conn.getInputStream());
            do {
                byte[] bs = new byte[1024];

                int len = input.read(bs);

                if (len == -1)
                    break;

                os.write(Arrays.copyOf(bs, len));
            } while (true);

            os.close();

            return os.toByteArray();
        } finally {
            if (out != null)
                out.close();

            if (input != null)
                input.close();

            if (conn != null)
                conn.disconnect();
        }
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
                Util.log(responseCode + " " + spec + " ");
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
