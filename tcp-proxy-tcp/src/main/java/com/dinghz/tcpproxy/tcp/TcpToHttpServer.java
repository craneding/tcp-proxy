/**
 *
 */
package com.dinghz.tcpproxy.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Scanner;

public class TcpToHttpServer {

    public static void main(String[] args) {
        TcpConfig.BASE_URL = args[0];
        TcpConfig.REMOTE_HOST = args[1];
        TcpConfig.REMOTE_PORT = Integer.valueOf(args[2]);
        TcpConfig.LOCAL_PORT = Integer.valueOf(args[3]);

        String logDir = System.getProperty("log.dir");
        if (logDir == null) {
            System.setProperty("log.dir", "./logs");
        }

        String proName = System.getProperty("pro.name");
        if (proName == null) {
            System.setProperty("pro.name", "" + TcpConfig.LOCAL_PORT);
        }

        final Logger logger = LoggerFactory.getLogger(TcpToHttpServer.class);

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

        TcpConfig.TCP_PROXY_PWD = "yitop@123456#qwerty";

        try {
            TcpServer.instance().start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
