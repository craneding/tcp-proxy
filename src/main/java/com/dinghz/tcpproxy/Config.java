package com.dinghz.tcpproxy;

import org.apache.commons.codec.digest.Md5Crypt;

import java.util.HashMap;
import java.util.Map;

/**
 * Config
 *
 * @author dinghz
 * @date 2017/5/17
 * @company 广州易站通计算机科技有限公司
 * @email dinghz@gzyitop.com
 */
public class Config {

    public static String salt = "$1$IDA/VORa$vzBWE5h/TGPxa3uGhs5SB.";
    public static String passwd = Md5Crypt.md5Crypt("yitop@123456#qwerty".getBytes(), Config.salt);
    public static Map<String, String> ips = new HashMap<>();

    static {
        ips.put("127.0.0.1", "dinghz");
        ips.put("192.168.30.246", "dinghz");
    }

}
