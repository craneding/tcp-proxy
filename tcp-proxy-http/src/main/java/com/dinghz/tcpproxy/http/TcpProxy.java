package com.dinghz.tcpproxy.http;

import com.dinghz.tcpproxy.Util;
import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class TcpProxy {

    private final String host;
    private final int port;
    private Socket socket;
    private InputStream input;
    private OutputStream out;

    public TcpProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 10 * 1000);
        socket.setSoTimeout(10 * 1000);
        socket.setKeepAlive(true);

        input = new BufferedInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());

        Util.log("创建连接:" + socket);
    }

    public void write(byte[] bs) throws IOException {
        if (out == null)
            throw new IOException("socket is closed.");

        boolean good = false;
        try {
            out.write(bs);
            out.flush();

            Util.log(">>" + Hex.encodeHexString(bs));

            good = true;
        } finally {
            if (good == false)
                disconnect();
        }
    }

    public byte[] read() throws IOException {
        byte[] bs = new byte[1024 * 3];
        //byte[] bs = new byte[256];

        if (input == null)
            throw new IOException("socket is closed.");

        boolean good = false;
        try {
            int len = input.read(bs);

            if (len == -1)
                throw new IOException("socket is closed.");

            good = true;

            bs = Arrays.copyOf(bs, len);

            Util.log("<<" + Hex.encodeHexString(bs));
        } catch (SocketTimeoutException e) {
            good = true;

            return null;
        } finally {
            if (good == false)
                disconnect();
        }

        return bs;
    }

    public synchronized void disconnect() throws IOException {
        if (input != null) {
            input.close();

            input = null;
        }

        if (out != null) {
            out.close();

            out = null;
        }

        if (socket != null) {
            socket.close();

            Util.log("关闭连接:" + socket);

            socket = null;
        }
    }
}
