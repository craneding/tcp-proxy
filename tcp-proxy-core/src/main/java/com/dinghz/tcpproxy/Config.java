package com.dinghz.tcpproxy;

/**
 * Config
 *
 * @author dinghz
 * @date 2017/5/17
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class Config {

    public static boolean TCP_PROXY_AUTH = false;
    public static String TCP_PROXY_AUTHCODE;

    static {
        if (System.getProperty("tcp_proxy_auth") != null) {
            Config.TCP_PROXY_AUTH = Boolean.valueOf(System.getProperty("tcp_proxy_auth"));
        } else if (System.getenv("tcp_proxy_auth") != null) {
            Config.TCP_PROXY_AUTH = Boolean.valueOf(System.getenv("tcp_proxy_auth"));
        }

        if (System.getProperty("tcp_proxy_authcode") != null) {
            Config.TCP_PROXY_AUTHCODE = System.getProperty("tcp_proxy_authcode");
        } else if (System.getenv("tcp_proxy_authcode") != null) {
            Config.TCP_PROXY_AUTHCODE = System.getenv("tcp_proxy_authcode");
        }
    }

}
