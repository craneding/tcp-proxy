package com.dinghz.tcpproxy.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

/**
 * @author dinghz
 * @date 2021/11/6
 * @company 广州车小Y科技有限公司
 * @email dinghz@gzyztd.com
 */
@Configuration
@SpringBootApplication
public class HttpApplication {

    static {
        String logDir = System.getProperty("log.dir");
        if (logDir == null) {
            System.setProperty("log.dir", "./logs");
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(HttpApplication.class, args);
    }

}
