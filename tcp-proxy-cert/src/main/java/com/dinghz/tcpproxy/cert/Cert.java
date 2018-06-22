package com.dinghz.tcpproxy.cert;

/**
 * Cert
 *
 * @author dinghz
 * @date 2018/6/21
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class Cert {

    static {
        System.loadLibrary("cert");
    }

    public static Cert getNewInstance() {
        return new Cert();
    }

    public native String version();

    public native String author();

    public native String passwd(String ip, String authCode);

    public native String hasCert(String ip, String passwd);

}
