package com.dinghz.tcpproxy.nat.client;

import com.dinghz.tcpproxy.nat.client.service.NatProxyClientInitializer;
import com.dinghz.tcpproxy.nat.client.service.NatTcpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * NatClientApplication
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@Configuration
@SpringBootApplication
public class NatClientApplication {
    private static String natProxyServerHost;
    private static Integer natProxyServerPort;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("usage: <proxy.server.host> <proxy.server.port>");

            System.exit(0);
        }

        natProxyServerHost = args[0];
        natProxyServerPort = Integer.valueOf(args[1]);

        SpringApplication.run(NatClientApplication.class, args);
    }

    @Bean
    public NatTcpClient natProxyClient() {
        NatTcpClient tcpClient = new NatTcpClient(natProxyServerHost, natProxyServerPort, new NatProxyClientInitializer());
        tcpClient.start();
        tcpClient.asyncConnect();

        return tcpClient;
    }

}
