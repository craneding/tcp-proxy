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
        //ips.put("127.0.0.1", "localhost");
        ips.put("192.168.30.246", "丁鹤泽");
        ips.put("192.168.30.248", "李振棠");
        ips.put("192.168.30.243", "黄炽桓");
        ips.put("192.168.30.247", "陈健");
        ips.put("192.168.30.245", "赵兴良");
        ips.put("192.168.30.58", "陈迪笋");
        ips.put("192.168.30.51", "冯振鹏");
        ips.put("192.168.40.119", "杨天龙");
        ips.put("192.168.40.115", "王刘杰");
        ips.put("192.168.30.61", "黄俊");
        ips.put("192.168.40.123", "何智锋");
    }

}
