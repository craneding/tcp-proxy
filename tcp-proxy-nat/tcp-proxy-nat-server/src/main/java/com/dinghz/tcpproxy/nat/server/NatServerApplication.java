package com.dinghz.tcpproxy.nat.server;

import com.dinghz.tcpproxy.nat.server.service.NatProxyServerInitializer;
import com.dinghz.tcpproxy.nat.server.service.NatRealMockServerInitializer;
import com.dinghz.tcpproxy.nat.server.service.Server;
import com.dinghz.tcpproxy.nat.server.service.ShareGroup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application
 *
 * @author dinghz
 * @date 2020/3/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@Configuration
@SpringBootApplication
public class NatServerApplication {

    private static Integer natProxyPort, realMockPort;
    private static String realServerHost;
    private static Integer realServerPort;

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("usage: <proxy.port> <mock.port> <real.server.host> <real.server.port>");

            System.exit(0);
        }

        natProxyPort = Integer.valueOf(args[0]);
        realMockPort = Integer.valueOf(args[1]);
        realServerHost = args[2];
        realServerPort = Integer.valueOf(args[3]);

        SpringApplication.run(NatServerApplication.class, args);
    }

    @Bean
    public ShareGroup shareGroup() {
        return new ShareGroup();
    }

    @Bean
    public Server serverNatProxy(ShareGroup shareGroup) {
        return new Server(natProxyPort, new NatProxyServerInitializer(shareGroup));
    }

    @Bean
    public Server serverRealMock(ShareGroup shareGroup) {
        System.out.println(realMockPort + " -> " + realServerHost + ":" + realServerPort);

        return new Server(realMockPort, new NatRealMockServerInitializer(realServerHost, realServerPort, shareGroup));
    }
}
